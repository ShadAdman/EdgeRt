package org.kmp.playground.kflite

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import java.io.File

actual class Interpreter actual constructor(model: ByteArray, options: InterpreterOptions) {
    private val context: Context by lazy { AppContext.get() }

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
        Runtime.TFLITE -> TFLiteInterpreterWrapper(model, options, context)
        Runtime.LITERT -> LiteRTInterpreterWrapper(model, options, context)
    }

    actual fun getInputTensorCount(): Int = wrapper.inputTensorCount
    actual fun getOutputTensorCount(): Int = wrapper.outputTensorCount
    actual fun getInputTensor(index: Int): Tensor = wrapper.getInputTensor(index)
    actual fun getOutputTensor(index: Int): Tensor = wrapper.getOutputTensor(index)
    actual fun resizeInput(index: Int, shape: TensorShape) = wrapper.resizeInput(index, shape.dimensions)
    actual fun run(inputs: List<Any>, outputs: Map<Int, Any>) = wrapper.run(inputs, outputs)
    actual fun close() = wrapper.close()

    private class TFLiteInterpreterWrapper(
        model: ByteArray,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val interpreter = org.tensorflow.lite.Interpreter(
            model.writeToTempFile(context),
            options.tensorFlowInterpreterOptions
        )

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
        model: ByteArray,
        options: InterpreterOptions,
        context: Context
    ) : PlatformInterpreterWrapper {
        private val env = Environment.create()
        private val modelFile = model.writeToTempFile(context)
        private val compiledModel = CompiledModel.create(
            modelFile.absolutePath,
            CompiledModel.Options(options.liteRTAccelerator),
            env
        )

        // For LiteRT 2.x, the API is quite different. 
        // For now, we provide a placeholder implementation for the wrapper 
        // to show how it would be structured.
        
        override val inputTensorCount: Int get() = 0 // CompiledModel handles buffers differently
        override val outputTensorCount: Int get() = 0
        override fun getInputTensor(index: Int): Tensor = throw UnsupportedOperationException("Not implemented for LiteRT yet")
        override fun getOutputTensor(index: Int): Tensor = throw UnsupportedOperationException("Not implemented for LiteRT yet")
        override fun resizeInput(index: Int, shape: IntArray) {}
        override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
             // In LiteRT 2.x, we should use TensorBuffers and compiledModel.run(...)
             println("Running with LiteRT CompiledModel (implementation pending)")
        }
        override fun close() {
            compiledModel.close()
            env.close()
        }
    }
}
