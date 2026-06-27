package org.kmp.playground.kflite

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

actual class Interpreter actual constructor(modelSource: ModelSource, options: InterpreterOptions) {
    private val context: Context by lazy { AppContext.get() }

    actual constructor(model: ByteArray, options: InterpreterOptions) : this(ByteArraySource(model), options)

    private interface PlatformInterpreterWrapper {
        val inputTensorCount: Int
        val outputTensorCount: Int
        fun getInputTensor(index: Int): Tensor
        fun getOutputTensor(index: Int): Tensor
        fun resizeInput(index: Int, shape: IntArray)
        fun run(inputs: List<Any>, outputs: Map<Int, Any>)
        fun close()
    }

    private val wrapper: PlatformInterpreterWrapper = when (options.runtime) {
        Runtime.TFLITE -> TFLiteInterpreterWrapper(modelSource, options, context)
        Runtime.LITERT -> LiteRTInterpreterWrapper(modelSource, options, context)
    }

    actual fun getInputTensorCount(): Int = wrapper.inputTensorCount
    actual fun getOutputTensorCount(): Int = wrapper.outputTensorCount
    actual fun getInputTensor(index: Int): Tensor = wrapper.getInputTensor(index)
    actual fun getOutputTensor(index: Int): Tensor = wrapper.getOutputTensor(index)
    actual fun resizeInput(index: Int, shape: TensorShape) = wrapper.resizeInput(index, shape.dimensions)
    actual fun run(inputs: List<Any>, outputs: Map<Int, Any>) = wrapper.run(inputs, outputs)
    actual fun close() = wrapper.close()

    private class TFLiteInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val interpreter: org.tensorflow.lite.Interpreter = when (modelSource) {
            is ByteArraySource -> org.tensorflow.lite.Interpreter(
                modelSource.bytes.writeToTempFile(context),
                options.tensorFlowInterpreterOptions
            )
            is FileSource -> {
                val file = File(modelSource.path)
                val inputStream = FileInputStream(file)
                val modelBuffer = inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    file.length()
                )
                org.tensorflow.lite.Interpreter(modelBuffer, options.tensorFlowInterpreterOptions)
            }
        }

        override val inputTensorCount: Int get() = interpreter.inputTensorCount
        override val outputTensorCount: Int get() = interpreter.outputTensorCount
        override fun getInputTensor(index: Int): Tensor = Tensor(interpreter.getInputTensor(index))
        override fun getOutputTensor(index: Int): Tensor = Tensor(interpreter.getOutputTensor(index))
        override fun resizeInput(index: Int, shape: IntArray) = interpreter.resizeInput(index, shape)
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            interpreter.runForMultipleInputsOutputs(inputs.toTypedArray(), outputs)
        }
        override fun close() = interpreter.close()
    }

    private class LiteRTInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val env = Environment.create()
        private val compiledModel: CompiledModel = when (modelSource) {
            is ByteArraySource -> {
                val modelFile = modelSource.bytes.writeToTempFile(context)
                CompiledModel.create(
                    modelFile.absolutePath,
                    options.liteRTInterpreterOptions,
                    env
                )
            }
            is FileSource -> {
                CompiledModel.create(
                    modelSource.path,
                    options.liteRTInterpreterOptions,
                    env
                )
            }
        }

        private val inputBuffers = compiledModel.createInputBuffers()
        private val outputBuffers = compiledModel.createOutputBuffers()

        override val inputTensorCount: Int get() = inputBuffers.size
        override val outputTensorCount: Int get() = outputBuffers.size
        override fun getInputTensor(index: Int): Tensor = Tensor(inputBuffers[index])
        override fun getOutputTensor(index: Int): Tensor = Tensor(outputBuffers[index])
        override fun resizeInput(index: Int, shape: IntArray) {
            println("Warning: resizeInput not supported in LiteRT CompiledModel wrapper.")
        }

        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            inputs.forEachIndexed { index, input ->
                if (index < inputBuffers.size) {
                    copyToLiteRTBuffer(inputBuffers[index], input)
                }
            }

            compiledModel.run(inputBuffers, outputBuffers)

            outputs.forEach { (index, outputContainer) ->
                if (index < outputBuffers.size) {
                    copyFromLiteRTBuffer(outputBuffers[index], outputContainer)
                }
            }
        }

        private fun copyToLiteRTBuffer(buffer: com.google.ai.edge.litert.TensorBuffer, input: Any) {
            when (input) {
                is FloatArray -> buffer.writeFloat(input)
                is IntArray -> buffer.writeInt(input)
                is ByteArray -> buffer.writeInt8(input)
                is LongArray -> buffer.writeLong(input)
                is BooleanArray -> buffer.writeBoolean(input)
                is ByteBuffer -> {
                    val array = ByteArray(input.remaining())
                    val pos = input.position()
                    input.get(array)
                    input.position(pos)
                    buffer.writeInt8(array)
                }
                is Array<*> -> {
                    when (val flatArray = flattenArray(input)) {
                        is FloatArray -> buffer.writeFloat(flatArray)
                        is IntArray -> buffer.writeInt(flatArray)
                        is ByteArray -> buffer.writeInt8(flatArray)
                        is LongArray -> buffer.writeLong(flatArray)
                        is BooleanArray -> buffer.writeBoolean(flatArray)
                    }
                }
            }
        }

        private fun copyFromLiteRTBuffer(buffer: com.google.ai.edge.litert.TensorBuffer, output: Any) {
            when (output) {
                is FloatArray -> buffer.readFloat().copyInto(output)
                is IntArray -> buffer.readInt().copyInto(output)
                is ByteArray -> buffer.readInt8().copyInto(output)
                is LongArray -> buffer.readLong().copyInto(output)
                is BooleanArray -> buffer.readBoolean().copyInto(output)
                is ByteBuffer -> {
                    val data = buffer.readInt8()
                    output.put(data)
                }
                is Array<*> -> {
                    val flatArray = when {
                        output.isArrayOf<FloatArray>() || (output.isNotEmpty() && (output[0] is FloatArray)) -> buffer.readFloat()
                        output.isArrayOf<IntArray>() || (output.isNotEmpty() && (output[0] is IntArray)) -> buffer.readInt()
                        output.isArrayOf<ByteArray>() || (output.isNotEmpty() && (output[0] is ByteArray)) -> buffer.readInt8()
                        output.isArrayOf<LongArray>() || (output.isNotEmpty() && (output[0] is LongArray)) -> buffer.readLong()
                        output.isArrayOf<BooleanArray>() || (output.isNotEmpty() && (output[0] is BooleanArray)) -> buffer.readBoolean()
                        output.isNotEmpty() && (output[0] is Array<*>) -> {
                             when (getFirstElement(output)) {
                                 is IntArray -> buffer.readInt()
                                 is ByteArray -> buffer.readInt8()
                                 is LongArray -> buffer.readLong()
                                 is BooleanArray -> buffer.readBoolean()
                                 else -> buffer.readFloat()
                             }
                        }
                        else -> buffer.readFloat()
                    }
                    unflattenArray(flatArray, output)
                }
            }
        }

        private fun flattenArray(input: Array<*>): Any {
            val totalSize = calculateTotalSize(input)
            val flat = when (getFirstElement(input)) {
                is FloatArray -> FloatArray(totalSize)
                is IntArray -> IntArray(totalSize)
                is ByteArray -> ByteArray(totalSize)
                is LongArray -> LongArray(totalSize)
                is BooleanArray -> BooleanArray(totalSize)
                else -> FloatArray(totalSize)
            }
            var offset = 0
            fun doFlatten(arr: Any) {
                if (arr is Array<*>) {
                    for (item in arr) {
                        if (item != null) doFlatten(item)
                    }
                } else {
                    val len = java.lang.reflect.Array.getLength(arr)
                    System.arraycopy(arr, 0, flat, offset, len)
                    offset += len
                }
            }
            doFlatten(input)
            return flat
        }

        private fun calculateTotalSize(input: Any): Int {
            return if (input is Array<*>) {
                var s = 0
                for (i in input) {
                    if (i != null) s += calculateTotalSize(i)
                }
                s
            } else {
                java.lang.reflect.Array.getLength(input)
            }
        }

        private fun getFirstElement(input: Any): Any {
            return if (input is Array<*> && input.isNotEmpty()) {
                getFirstElement(input[0]!!)
            } else {
                input
            }
        }

        private fun unflattenArray(src: Any, dest: Any, offset: Int = 0): Int {
            var currentOffset = offset
            if (dest is Array<*>) {
                for (item in dest) {
                    if (item != null) {
                        currentOffset = unflattenArray(src, item, currentOffset)
                    }
                }
            } else {
                val len = java.lang.reflect.Array.getLength(dest)
                System.arraycopy(src, offset, dest, 0, len)
                currentOffset += len
            }
            return currentOffset
        }

        override fun close() {
            inputBuffers.forEach { it.close() }
            outputBuffers.forEach { it.close() }
            compiledModel.close()
            env.close()
        }
    }
}
