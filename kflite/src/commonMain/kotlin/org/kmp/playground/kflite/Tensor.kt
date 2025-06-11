package org.kmp.playground.kflite

expect class Tensor {
    val dataType: TensorDataType
    val name: String
    val shape: IntArray
}