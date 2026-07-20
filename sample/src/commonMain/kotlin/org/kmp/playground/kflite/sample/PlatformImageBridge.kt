package org.kmp.playground.kflite.sample

import androidx.compose.ui.graphics.ImageBitmap
import org.kmp.playground.kflite.preprocessing.image.*

expect fun ImageBitmap.toPlatformImage(): PlatformImage

fun ImageBitmap.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer = toPlatformImage().preprocess(allocateSize, block)

fun ImageBitmap.imageToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean = false
): TensorBuffer = toPlatformImage().imageToScaledByteBuffer(inputWidth, inputHeight, inputAllocateSize, normalize)
