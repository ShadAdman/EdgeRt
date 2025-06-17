package org.kmp.playground.kflite

import android.content.Context
import java.io.File
import kotlin.text.get

actual class Interpreter actual constructor(model: ByteArray, options: InterpreterOptions) {
    private val context: Context by lazy { AppContext.get() }

    private val tensorFlowInterpreter = PlatformInterpreter(
        model.writeToTempFile(context),
        options.tensorFlowInterpreterOptions
    )

    /**
     * Gets the number of input tensors.
     */
    actual fun getInputTensorCount(): Int = tensorFlowInterpreter.inputTensorCount

    /**
     * Gets the number of output Tensors.
     */
    actual fun getOutputTensorCount(): Int = tensorFlowInterpreter.outputTensorCount

    /**
     * Gets the Tensor associated with the provided input index.
     *
     * @throws IllegalArgumentException if [index] is negative or is not smaller than the
     * number of model inputs.
     */
    actual fun getInputTensor(index: Int): Tensor = tensorFlowInterpreter.getInputTensor(index).toTensor()

    /**
     * Gets the Tensor associated with the provided output index.
     *
     * @throws IllegalArgumentException if [index] is negative or is not smaller than the
     * number of model inputs.
     */
    actual fun getOutputTensor(index: Int): Tensor = tensorFlowInterpreter.getOutputTensor(index).toTensor()

    /**
     * Resizes [index] input of the native model to the given [shape].
     */
    actual fun resizeInput(index: Int, shape: TensorShape) {
        tensorFlowInterpreter.resizeInput(index, shape.dimensions)
    }

    /**
     * Runs model inference if the model takes multiple inputs, or returns multiple outputs.
     */
    actual fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
        println("Input size: ${inputs.toTypedArray().take(10).joinToString()}")
        tensorFlowInterpreter.runForMultipleInputsOutputs(inputs.toTypedArray(), outputs)
    }

    /**
     * Release resources associated with the [Interpreter].
     */
    actual fun close() {
        tensorFlowInterpreter.close()
    }
}