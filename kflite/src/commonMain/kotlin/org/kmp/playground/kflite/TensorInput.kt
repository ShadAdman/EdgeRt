package org.kmp.playground.kflite

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.Any

typealias TensorBuffer = Any

expect fun ImageBitmap.imageToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean = false
): TensorBuffer


expect fun ByteArray.bytesToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean = false
): TensorBuffer