package org.kmp.playground.kflite.tensor

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

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

expect class Tensor(runtimeTensor: RuntimeTensor) {
    val dataType: TensorDataType
    val name: String
    val shape: IntArray
}






