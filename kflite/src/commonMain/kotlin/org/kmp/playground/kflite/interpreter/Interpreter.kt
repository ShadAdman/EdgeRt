package org.kmp.playground.kflite.interpreter

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

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



