package org.kmp.playground.kflite.core.interpreter

import org.kmp.playground.kflite.core.model.*
import org.kmp.playground.kflite.core.runtime.Runtime
import org.kmp.playground.kflite.core.runtime.RuntimeFactory

expect class Interpreter(modelSource: ModelSource, options: InterpreterOptions) {
    constructor(model: ByteArray, options: InterpreterOptions)
    fun getInputTensorCount(): Int
    fun getOutputTensorCount(): Int
    fun getInputTensor(index: Int): Tensor
    fun getOutputTensor(index: Int): Tensor
    fun resizeInput(index: Int, shape: TensorShape)
    fun run(inputs: List<Any>, outputs: Map<Int, Any>)
    fun getMetadata(): ModelMetadata
    fun close()
}
