package org.kmp.playground.kflite

import androidx.compose.ui.graphics.ImageBitmap

actual fun ImageBitmap.toScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val uiImage = this.toUIImage()
    checkNotNull(uiImage) { "Failed to convert ImageBitmap to UIImage" }
    val scaledImage = uiImage.scaleTo(inputWidth, inputHeight)
    checkNotNull(scaledImage) { "Failed to scale UIImage" }
    val pixelData = scaledImage.toRGBByteArray(normalize)
    checkNotNull(pixelData) { "Failed to extract RGB byte array" }

    println("RGB byte array size: ${pixelData.size}")
    println("First 10 bytes: ${pixelData.take(10)}")
    return pixelData.toNSData()
}
