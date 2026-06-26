package org.kmp.playground.kflite.runtime.litert

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.Accelerator
import org.kmp.playground.kflite.core.model.*
import org.kmp.playground.kflite.core.runtime.Runtime
import org.kmp.playground.kflite.core.interpreter.InterpreterOptions
import org.kmp.playground.kflite.core.delegate.DelegateType
import org.kmp.playground.kflite.core.writeToTempFile
import java.io.File
import java.nio.ByteBuffer

class AndroidLiteRtRuntime(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions,
    private val context: Context
) : Runtime {
    private val env = Environment.create()
    private val compiledModel: CompiledModel by lazy {
        when (modelSource) {
            is ByteArraySource -> {
                val modelFile = modelSource.bytes.writeToTempFile(context)
                CompiledModel.create(
                    modelFile.absolutePath,
                    CompiledModel.Options(getAccelerator(options.delegateType)),
                    env
                )
            }
            is FileSource -> {
                CompiledModel.create(
                    modelSource.path,
                    CompiledModel.Options(getAccelerator(options.delegateType)),
                    env
                )
            }
        }
    }

    private val inputBuffers by lazy { compiledModel.createInputBuffers() }
    private val outputBuffers by lazy { compiledModel.createOutputBuffers() }

    override val inputTensorCount: Int get() = inputBuffers.size
    override val outputTensorCount: Int get() = outputBuffers.size

    override fun getInputTensor(index: Int): Tensor {
        return Tensor(LiteRtRuntimeTensor(inputBuffers[index]))
    }

    override fun getOutputTensor(index: Int): Tensor {
        return Tensor(LiteRtRuntimeTensor(outputBuffers[index]))
    }

    override fun resizeInput(index: Int, shape: IntArray) {
        println("Warning: resizeInput not supported in LiteRT CompiledModel runtime yet.")
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

    override fun getMetadata(): ModelMetadata {
        val inputs = inputBuffers.mapIndexed { index, _ ->
            TensorMetadata("input_$index", IntArray(0), TensorDataType.FLOAT32)
        }
        val outputs = outputBuffers.mapIndexed { index, _ ->
            TensorMetadata("output_$index", IntArray(0), TensorDataType.FLOAT32)
        }
        return ModelMetadata(null, null, null, null, null, null, inputs, outputs)
    }

    override fun close() {
        inputBuffers.forEach { it.close() }
        outputBuffers.forEach { it.close() }
        compiledModel.close()
        env.close()
    }

    private fun getAccelerator(type: DelegateType): Accelerator = when (type) {
        DelegateType.CPU -> Accelerator.CPU
        DelegateType.GPU_METAL -> Accelerator.GPU
        DelegateType.NNAPI_COREML -> Accelerator.NPU
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
                    output.isNotEmpty() && output[0] is Array<*> -> {
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

    private class LiteRtRuntimeTensor(private val buffer: com.google.ai.edge.litert.TensorBuffer) : RuntimeTensor {
        override val dataType: TensorDataType get() = TensorDataType.FLOAT32 // FIXME
        override val name: String get() = "LiteRT Tensor"
        override val shape: IntArray get() = IntArray(0) // FIXME
    }
}
