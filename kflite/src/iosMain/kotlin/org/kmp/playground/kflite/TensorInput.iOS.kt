package org.kmp.playground.kflite

import androidx.compose.ui.graphics.ImageBitmap
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.Foundation.create
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

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
    println("First 10 bytes of RGB byte array: ${pixelData.take(10)}")
    return pixelData.toNSData()
}



actual fun ByteArray.toScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val uiImage = this.usePinned { pinned ->
        val data = NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
        UIImage.imageWithData(data)
    } ?: throw IllegalArgumentException("Could not decode ByteArray to UIImage")

    val scaledImage = uiImage.scaleTo(inputWidth, inputHeight) // Using your existing extension
        ?: throw IllegalStateException("Failed to scale UIImage")

    val pixelData = scaledImage.toRGBByteArray(normalize) // Using your existing extension
        ?: throw IllegalStateException("Failed to extract RGB byte array")

    return pixelData.toNSData()
}
