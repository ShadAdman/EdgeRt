package org.kmp.playground.edgert.interpreter

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*


import kotlinx.cinterop.*
import platform.Foundation.NSData


@OptIn(ExperimentalForeignApi::class)
actual class Interpreter actual constructor(modelSource: ModelSource, options: InterpreterOptions) {

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
        RuntimeType.TFLITE -> TFLiteInterpreterWrapper(modelSource, options)
        RuntimeType.LITERT -> LiteRTInterpreterWrapper(modelSource, options)
        RuntimeType.EXECUTORCH -> ExecuTorchInterpreterWrapper(modelSource, options)
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
        options: InterpreterOptions
    ) : PlatformInterpreterWrapper {
        private var tflInterpreter: PlatformInterpreter? = null

        init {
            tflInterpreter = errorHandled { errPtr ->
                when (modelSource) {
                    is ByteArraySource -> PlatformInterpreter(modelSource.bytes.writeToTempFile(), options = options.tflInterpreterOptions, error = errPtr)
                    is FileSource -> PlatformInterpreter(modelSource.path, options = options.tflInterpreterOptions, error = errPtr)
                    is ResourceSource, is AssetSource -> {
                        val bundle = platform.Foundation.NSBundle.mainBundle
                        val fullPath = (modelSource as? ResourceSource)?.path ?: (modelSource as AssetSource).path
                        val fileName = fullPath.substringAfterLast("/")
                        val resourceName = fileName.substringBeforeLast(".")
                        val extension = fileName.substringAfterLast(".", "")
                        val subDir = fullPath.substringBeforeLast("/", "").takeIf { it.isNotEmpty() }
                        val path = bundle.pathForResource(resourceName, extension, subDir)
                            ?: bundle.pathForResource(fileName, null)
                            ?: error("Resource not found: $fullPath")
                        PlatformInterpreter(path, options = options.tflInterpreterOptions, error = errPtr)
                    }
                }
            }!!
            errorHandled { errPtr ->
                val interpreter = requireNotNull(tflInterpreter) { "Interpreter has been closed or not initialized." }
                interpreter.allocateTensorsWithError(errPtr)
            }
        }

        override val inputTensorCount: Int get() = tflInterpreter?.inputTensorCount()?.toInt() ?: 0
        override val outputTensorCount: Int get() = tflInterpreter?.outputTensorCount()?.toInt() ?: 0

        override fun getInputTensor(index: Int): Tensor {
            val t = tflInterpreter?.inputTensorAtIndex(index.toULong(), null)!!
            return Tensor(t)
        }

        override fun getOutputTensor(index: Int): Tensor {
            val t = tflInterpreter?.outputTensorAtIndex(index.toULong(), null)!!
            return Tensor(t)
        }

        override fun resizeInput(index: Int, shape: IntArray) { /* Logic */ }
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) { /* Logic */ }

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

        override fun close() {
            tflInterpreter = null
        }
    }

    private class LiteRTInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions
    ) : PlatformInterpreterWrapper {
        private val delegate = TFLiteInterpreterWrapper(modelSource, options)

        override val inputTensorCount: Int get() = delegate.inputTensorCount
        override val outputTensorCount: Int get() = delegate.outputTensorCount
        override fun getInputTensor(index: Int): Tensor = delegate.getInputTensor(index)
        override fun getOutputTensor(index: Int): Tensor = delegate.getOutputTensor(index)
        override fun resizeInput(index: Int, shape: IntArray) = delegate.resizeInput(index, shape)
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) = delegate.run(inputs, outputs)
        override fun getMetadata(): ModelMetadata = delegate.getMetadata()
        override fun close() = delegate.close()
    }

    private class ExecuTorchInterpreterWrapper(
        modelSource: ModelSource,
        options: InterpreterOptions
    ) : PlatformInterpreterWrapper {
        private var module: PlatformPytorchModule? = null

        init {
            val path = when (modelSource) {
                is ByteArraySource -> modelSource.bytes.writeToTempFile()
                is FileSource -> modelSource.path
                is ResourceSource, is AssetSource -> {
                    val bundle = platform.Foundation.NSBundle.mainBundle
                    val fullPath = (modelSource as? ResourceSource)?.path ?: (modelSource as AssetSource).path
                    val fileName = fullPath.substringAfterLast("/")
                    val resourceName = fileName.substringBeforeLast(".")
                    val extension = fileName.substringAfterLast(".", "")
                    val subDir = fullPath.substringBeforeLast("/", "").takeIf { it.isNotEmpty() }
                    bundle.pathForResource(resourceName, extension, subDir)
                        ?: bundle.pathForResource(fileName, null)
                        ?: error("Resource not found: $fullPath")
                }
            }
            module = PlatformPytorchModule(filePath = path)
        }

        override val inputTensorCount: Int get() = 0
        override val outputTensorCount: Int get() = 0

        override fun getInputTensor(index: Int): Tensor {
            throw UnsupportedOperationException("ExecuTorch doesn't expose input tensor info directly via ObjC API.")
        }

        override fun getOutputTensor(index: Int): Tensor {
            throw UnsupportedOperationException("ExecuTorch doesn't expose output tensor info directly via ObjC API.")
        }

        override fun resizeInput(index: Int, shape: IntArray) {
            // Not supported
        }

        @OptIn(ExperimentalForeignApi::class)
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
            val eValues = inputs.map { toEValue(it) }
            val results = errorHandled { errPtr ->
                module?.forward(eValues, errPtr)
            } as? List<*> ?: emptyList()

            outputs.forEach { (index, outputContainer) ->
                val result = results.getOrNull(index) as? PlatformPytorchEValue
                if (result != null) {
                    fromEValue(result, outputContainer)
                }
            }
        }

        override fun getMetadata(): ModelMetadata {
            return ModelMetadata(null, null, null, null, null, null, emptyList(), emptyList())
        }

        override fun close() {
            module = null
        }

        @OptIn(ExperimentalForeignApi::class)
        private fun toEValue(input: Any): PlatformPytorchEValue {
            return when (input) {
                is FloatArray -> {
                    val tensor = PlatformPytorchTensor(input.toNSData(), listOf(input.size.toLong()), 0)
                    PlatformPytorchEValue.withTensor(tensor)
                }
                is IntArray -> {
                    val tensor = PlatformPytorchTensor(input.toNSData(), listOf(input.size.toLong()), 1)
                    PlatformPytorchEValue.withTensor(tensor)
                }
                is Float -> PlatformPytorchEValue.withDouble(input.toDouble())
                is Int -> PlatformPytorchEValue.withInt(input.toLong())
                is Boolean -> PlatformPytorchEValue.withBool(input)
                is NSData -> {
                    val size = input.length.toLong() / 4
                    val tensor = PlatformPytorchTensor(input, listOf(size), 0) // Assume Float32
                    PlatformPytorchEValue.withTensor(tensor)
                }
                else -> throw IllegalArgumentException("Unsupported input type for ExecuTorch: ${input::class.simpleName}")
            }
        }

        @OptIn(ExperimentalForeignApi::class)
        private fun fromEValue(eValue: PlatformPytorchEValue, outputContainer: Any) {
            if (eValue.isTensor()) {
                val tensor = eValue.toTensor()
                val data = tensor.data()
                when (outputContainer) {
                    is FloatArray -> data.toFloatArray().copyInto(outputContainer)
                    is IntArray -> data.toIntArray().copyInto(outputContainer)
                    is ByteArray -> data.toByteArray().copyInto(outputContainer)
                    else -> throw IllegalArgumentException("Unsupported output type for ExecuTorch Tensor: ${outputContainer::class.simpleName}")
                }
            } else if (eValue.isBool()) {
                if (outputContainer is BooleanArray && outputContainer.isNotEmpty()) {
                    outputContainer[0] = eValue.toBool()
                }
            } else if (eValue.isInt()) {
                if (outputContainer is LongArray && outputContainer.isNotEmpty()) {
                    outputContainer[0] = eValue.toInt()
                }
            } else if (eValue.isDouble()) {
                if (outputContainer is DoubleArray && outputContainer.isNotEmpty()) {
                    outputContainer[0] = eValue.toDouble()
                }
            }
        }
    }
}





