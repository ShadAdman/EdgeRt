package org.kmp.playground.kflite

import androidx.compose.ui.graphics.ImageBitmap

actual fun ImageBitmap.toScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val uiImage = this.toUIImage()
    val scaledImage = uiImage?.scaleTo(inputWidth, inputHeight)
    val pixelData = scaledImage?.toRGBByteArray(normalize)

    return pixelData?:"Error scaling image in ios"
}
