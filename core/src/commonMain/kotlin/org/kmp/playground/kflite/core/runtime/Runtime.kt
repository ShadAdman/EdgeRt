package org.kmp.playground.kflite.core.runtime

import org.kmp.playground.kflite.core.model.Tensor
import org.kmp.playground.kflite.core.model.ModelMetadata

interface Runtime {
    val inputTensorCount: Int
    val outputTensorCount: Int
    fun getInputTensor(index: Int): Tensor
    fun getOutputTensor(index: Int): Tensor
    fun resizeInput(index: Int, shape: IntArray)
    fun run(inputs: List<Any>, outputs: Map<Int, Any>)
    fun getMetadata(): ModelMetadata
    fun close()
}
