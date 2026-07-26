package org.kmp.playground.edgert.interpreter

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

expect class Interpreter(modelSource: ModelSource, options: InterpreterOptions) {
    constructor(model: ByteArray, options: InterpreterOptions)
    fun getInputTensorCount(): Int
    fun getOutputTensorCount(): Int
    fun getInputTensor(index: Int): Tensor
    fun getOutputTensor(index: Int): Tensor
    fun resizeInput(index: Int, shape: TensorShape)
    fun run(inputs: List<Any>, outputs: Map<Int, Any>)
    fun warmUp(config: WarmUpConfig)
    fun wakeUp()
    fun getMetadata(): ModelMetadata
    fun close()
}





