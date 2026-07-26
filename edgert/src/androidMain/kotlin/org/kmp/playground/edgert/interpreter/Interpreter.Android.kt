package org.kmp.playground.edgert.interpreter

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

import android.content.Context
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.Accelerator
import org.kmp.playground.edgert.AppContext
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Module as PytorchModule
import org.pytorch.executorch.Tensor as PytorchTensor
import org.tensorflow.lite.DataType as TFLiteDataType
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

actual class Interpreter actual constructor(modelSource: ModelSource, options: InterpreterOptions) {
    private val context: Context by lazy { AppContext.get() }

    private interface PlatformInterpreterWrapper {
        val inputTensorCount: Int
        val outputTensorCount: Int
        fun getInputTensor(index: Int): Tensor
        fun getOutputTensor(index: Int): Tensor
        fun resizeInput(index: Int, shape: IntArray)
        fun run(inputs: List<Any>, outputs: Map<Int, Any>)
        fun getMetadata(): ModelMetadata
        fun close()
    }

    private val wrapper: PlatformInterpreterWrapper = when (options.runtime) {
        RuntimeType.TFLITE -> TFLiteInterpreterWrapper(modelSource, options, context)
        RuntimeType.LITERT -> LiteRTInterpreterWrapper(modelSource, options, context)
        RuntimeType.EXECUTORCH -> ExecutorchInterpreterWrapper(modelSource, options, context)
    }

    actual constructor(model: ByteArray, options: InterpreterOptions) : this(ByteArraySource(model), options)

    actual fun getInputTensorCount(): Int = wrapper.inputTensorCount
    actual fun getOutputTensorCount(): Int = wrapper.outputTensorCount
    actual fun getInputTensor(index: Int): Tensor = wrapper.getInputTensor(index)
    actual fun getOutputTensor(index: Int): Tensor = wrapper.getOutputTensor(index)
    actual fun resizeInput(index: Int, shape: TensorShape) = wrapper.resizeInput(index, shape.dimensions)
    actual fun run(inputs: List<Any>, outputs: Map<Int, Any>) = wrapper.run(inputs, outputs)
    actual fun warmUp(config: WarmUpConfig) {
        WarmUpEngine(this, config).warmUp()
    }
    actual fun wakeUp() {
        WarmUpEngine(this).wakeUp()
    }
    actual fun getMetadata(): ModelMetadata = wrapper.getMetadata()
    actual fun close() = wrapper.close()

    private class TFLiteInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val interpreter: PlatformTFLiteInterpreter = when (modelSource) {
            is ByteArraySource -> PlatformTFLiteInterpreter(
                modelSource.bytes.writeToTempFile(context),
                options.tensorFlowInterpreterOptions
            )
            is FileSource -> {
                val file = File(modelSource.path)
                val inputStream = FileInputStream(file)
                val modelBuffer = inputStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
                PlatformTFLiteInterpreter(modelBuffer, options.tensorFlowInterpreterOptions)
            }
            is AssetSource -> {
                val fileDescriptor = context.assets.openFd(modelSource.path)
                val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
                val modelBuffer = inputStream.channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
                PlatformTFLiteInterpreter(modelBuffer, options.tensorFlowInterpreterOptions)
            }
            is ResourceSource -> {
                val fileDescriptor = context.assets.openFd(modelSource.path)
                val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
                val modelBuffer = inputStream.channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
                PlatformTFLiteInterpreter(modelBuffer, options.tensorFlowInterpreterOptions)
            }
        }

        override val inputTensorCount: Int get() = interpreter.inputTensorCount
        override val outputTensorCount: Int get() = interpreter.outputTensorCount
        override fun getInputTensor(index: Int): Tensor = Tensor(TfliteRuntimeTensor(interpreter.getInputTensor(index)))
        override fun getOutputTensor(index: Int): Tensor = Tensor(TfliteRuntimeTensor(interpreter.getOutputTensor(index)))
        override fun resizeInput(index: Int, shape: IntArray) = interpreter.resizeInput(index, shape)
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            interpreter.runForMultipleInputsOutputs(inputs.toTypedArray(), outputs)
        }

        override fun close() = interpreter.close()

        override fun getMetadata(): ModelMetadata {
            val inputs = (0 until inputTensorCount).map { i ->
                val t = getInputTensor(i)
                TensorMetadata(t.name, t.shape, t.dataType)
            }
            val outputs = (0 until outputTensorCount).map { i ->
                val t = getOutputTensor(i)
                TensorMetadata(t.name, t.shape, t.dataType)
            }
            return ModelMetadata(null, null, null, null, null, null, inputs, outputs)
        }

        private class TfliteRuntimeTensor(private val tflTensor: PlatformTFLiteTensor) : RuntimeTensor {
            override val dataType: TensorDataType get() = tflTensor.dataType().toCoreTensorDataType()
            override val name: String get() = tflTensor.name()
            override val shape: IntArray get() = tflTensor.shape()
        }
    }

    private class LiteRTInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val env = Environment.create()
        private val compiledModel: PlatformLiteRTCompiledModel = when (modelSource) {
            is ByteArraySource -> {
                val modelFile = modelSource.bytes.writeToTempFile(context)
                PlatformLiteRTCompiledModel.create(modelFile.absolutePath, options.liteRTInterpreterOptions, env)
            }
            is FileSource -> PlatformLiteRTCompiledModel.create(modelSource.path, options.liteRTInterpreterOptions, env)
            is AssetSource -> PlatformLiteRTCompiledModel.create(context.assets, modelSource.path, options.liteRTInterpreterOptions, env)
            is ResourceSource -> PlatformLiteRTCompiledModel.create(context.assets, modelSource.path, options.liteRTInterpreterOptions, env)
        }

        private val inputBuffers by lazy { compiledModel.createInputBuffers() }
        private val outputBuffers by lazy { compiledModel.createOutputBuffers() }

        override val inputTensorCount: Int get() = inputBuffers.size
        override val outputTensorCount: Int get() = outputBuffers.size
        override fun getInputTensor(index: Int): Tensor = Tensor(LiteRtRuntimeTensor(inputBuffers[index]))
        override fun getOutputTensor(index: Int): Tensor = Tensor(LiteRtRuntimeTensor(outputBuffers[index]))
        override fun resizeInput(index: Int, shape: IntArray) {
            println("Warning: resizeInput not supported in LiteRT CompiledModel runtime yet.")
        }
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            inputs.forEachIndexed { index, input ->
                if (index < inputBuffers.size) copyToLiteRTBuffer(inputBuffers[index], input)
            }
            compiledModel.run(inputBuffers, outputBuffers)
            outputs.forEach { (index, outputContainer) ->
                if (index < outputBuffers.size) copyFromLiteRTBuffer(outputBuffers[index], outputContainer)
            }
        }
        override fun close() {
            inputBuffers.forEach { it.close() }
            outputBuffers.forEach { it.close() }
            compiledModel.close()
            env.close()
        }

        override fun getMetadata(): ModelMetadata {
             val inputs = (0 until inputTensorCount).map { i ->
                val t = getInputTensor(i)
                TensorMetadata(t.name, t.shape, t.dataType)
            }
            val outputs = (0 until outputTensorCount).map { i ->
                val t = getOutputTensor(i)
                TensorMetadata(t.name, t.shape, t.dataType)
            }
            return ModelMetadata(null, null, null, null, null, null, inputs, outputs)
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

    private class ExecutorchInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val module: PytorchModule = when (modelSource) {
            is ByteArraySource -> PytorchModule.load(modelSource.bytes.writeToTempFile(context, suffix = ".pte").absolutePath)
            is FileSource -> PytorchModule.load(modelSource.path)
            is AssetSource -> {
                val file = File(context.cacheDir, modelSource.path.substringAfterLast("/"))
                context.assets.open(modelSource.path).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                PytorchModule.load(file.absolutePath)
            }
            is ResourceSource -> {
                val file = File(context.cacheDir, modelSource.path.substringAfterLast("/"))
                context.assets.open(modelSource.path).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                PytorchModule.load(file.absolutePath)
            }
        }

        override val inputTensorCount: Int get() = 0 // Not available in Java API
        override val outputTensorCount: Int get() = 0 // Not available in Java API

        override fun getInputTensor(index: Int): Tensor {
            throw UnsupportedOperationException("ExecuTorch doesn't expose input tensor info directly via Java API.")
        }

        override fun getOutputTensor(index: Int): Tensor {
            throw UnsupportedOperationException("ExecuTorch doesn't expose output tensor info directly via Java API.")
        }

        override fun resizeInput(index: Int, shape: IntArray) {
            // Not supported
        }

        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            println("pytorch runtime started")
            val eValues = inputs.map { input ->
                toEValue(input)
            }.toTypedArray()

            val results = module.forward(*eValues)

            outputs.forEach { (index, outputContainer) ->
                if (index < results.size) {
                    fromEValue(results[index], outputContainer)
                }
            }
        }

        private fun toEValue(input: Any): EValue {
            return when (input) {
                is FloatArray -> EValue.from(PytorchTensor.fromBlob(input, longArrayOf(input.size.toLong())))
                is IntArray -> EValue.from(PytorchTensor.fromBlob(input, longArrayOf(input.size.toLong())))
                is LongArray -> EValue.from(PytorchTensor.fromBlob(input, longArrayOf(input.size.toLong())))
                is ByteArray -> EValue.from(PytorchTensor.fromBlob(input, longArrayOf(input.size.toLong())))
                is Float -> EValue.from(input.toDouble())
                is Int -> EValue.from(input.toLong())
                is Long -> EValue.from(input)
                is Boolean -> EValue.from(input)
                is String -> EValue.from(input)
                is ByteBuffer -> {
                    // Assuming the buffer contains float32 data if it's coming from imageToScaledByteBuffer(normalize=true)
                    // We need the shape. For now, assume a flat shape or use the buffer's capacity.
                    val size = input.capacity() / 4 // 4 bytes for Float32
                    EValue.from(PytorchTensor.fromBlob(input, longArrayOf(size.toLong())))
                }
                else -> throw IllegalArgumentException("Unsupported input type for ExecuTorch: ${input::class.simpleName}")
            }
        }

        private fun fromEValue(eValue: EValue, outputContainer: Any) {
            if (eValue.isTensor) {
                val tensor = eValue.toTensor()
                when (outputContainer) {
                    is FloatArray -> tensor.getDataAsFloatArray().copyInto(outputContainer)
                    is IntArray -> tensor.getDataAsIntArray().copyInto(outputContainer)
                    is LongArray -> tensor.getDataAsLongArray().copyInto(outputContainer)
                    is ByteArray -> tensor.getDataAsByteArray().copyInto(outputContainer)
                    else -> throw IllegalArgumentException("Unsupported output type for ExecuTorch Tensor: ${outputContainer::class.simpleName}")
                }
            } else if (eValue.isBool) {
                if (outputContainer is BooleanArray && outputContainer.isNotEmpty()) {
                    outputContainer[0] = eValue.toBool()
                }
            } else if (eValue.isInt) {
                if (outputContainer is LongArray && outputContainer.isNotEmpty()) {
                    outputContainer[0] = eValue.toInt()
                }
            } else if (eValue.isDouble) {
                if (outputContainer is DoubleArray && outputContainer.isNotEmpty()) {
                    outputContainer[0] = eValue.toDouble()
                }
            } else if (eValue.isString) {
                println("Warning: ExecuTorch String output not supported yet in EdgeRt wrapper.")
            }
        }

        override fun getMetadata(): ModelMetadata {
            return ModelMetadata(null, null, null, null, null, null, emptyList(), emptyList())
        }

        override fun close() {
            module.close()
        }
    }
}

private fun TFLiteDataType.toCoreTensorDataType(): TensorDataType = when (this) {
    TFLiteDataType.FLOAT32 -> TensorDataType.FLOAT32
    TFLiteDataType.INT32 -> TensorDataType.INT32
    TFLiteDataType.UINT8 -> TensorDataType.UINT8
    TFLiteDataType.INT64 -> TensorDataType.INT64
    else -> throw IllegalArgumentException("Unsupported TFLite data type: $this")
}





