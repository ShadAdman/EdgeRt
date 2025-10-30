package org.kmp.playground.kflite

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

value class TensorShape(
    val dimensions: IntArray
) {
    val rank: Int
        get() = dimensions.size
}
