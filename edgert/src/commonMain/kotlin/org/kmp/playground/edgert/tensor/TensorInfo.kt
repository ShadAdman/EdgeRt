package org.kmp.playground.edgert.tensor

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

import kotlin.jvm.JvmInline

@Suppress("MagicNumber")
enum class TensorDataType(val value: Int) {
    FLOAT32(1),
    INT32(2),
    UINT8(3),
    INT64(4);

    fun byteSize(): Int = when (this) {
        FLOAT32 -> 4
        INT32 -> 4
        UINT8 -> 1
        INT64 -> 8
    }
}

@JvmInline
value class TensorShape(
    val dimensions: IntArray
) {
    val rank: Int
        get() = dimensions.size
}

interface RuntimeTensor {
    val dataType: TensorDataType
    val name: String
    val shape: IntArray
}

expect class Tensor {
    val dataType: TensorDataType
    val name: String
    val shape: IntArray
}








