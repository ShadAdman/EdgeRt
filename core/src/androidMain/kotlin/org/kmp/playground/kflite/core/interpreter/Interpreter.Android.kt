package org.kmp.playground.kflite.core.interpreter

import org.kmp.playground.kflite.core.model.*
import org.kmp.playground.kflite.core.runtime.Runtime
import org.kmp.playground.kflite.core.runtime.RuntimeFactory

actual class Interpreter actual constructor(modelSource: ModelSource, options: InterpreterOptions) {
    private val runtime: Runtime = RuntimeFactory.create(modelSource, options)

    actual constructor(model: ByteArray, options: InterpreterOptions) : this(ByteArraySource(model), options)

    actual fun getInputTensorCount(): Int = runtime.inputTensorCount
    actual fun getOutputTensorCount(): Int = runtime.outputTensorCount
    actual fun getInputTensor(index: Int): Tensor = runtime.getInputTensor(index)
    actual fun getOutputTensor(index: Int): Tensor = runtime.getOutputTensor(index)
    actual fun resizeInput(index: Int, shape: TensorShape) = runtime.resizeInput(index, shape.dimensions)
    actual fun run(inputs: List<Any>, outputs: Map<Int, Any>) = runtime.run(inputs, outputs)
    actual fun getMetadata(): ModelMetadata = runtime.getMetadata()
    actual fun close() = runtime.close()
}
