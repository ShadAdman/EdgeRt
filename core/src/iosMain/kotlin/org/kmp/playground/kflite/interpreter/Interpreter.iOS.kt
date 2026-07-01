package org.kmp.playground.kflite.interpreter

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

import kotlinx.cinterop.*

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
}



