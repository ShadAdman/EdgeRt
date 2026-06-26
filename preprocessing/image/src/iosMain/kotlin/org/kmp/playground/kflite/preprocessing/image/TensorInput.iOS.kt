package org.kmp.playground.kflite.preprocessing.image

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.Foundation.create
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

actual fun ImageBitmap.imageToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val uiImage = this.toUIImage()
    checkNotNull(uiImage) { "Failed to convert ImageBitmap to UIImage" }
    val scaledImage = uiImage.scaleTo(inputWidth, inputHeight)
    val pixelData = scaledImage.toRGBByteArray(normalize)

    println("RGB byte array size: ${pixelData.size}")
    println("First 10 bytes of RGB byte array: ${pixelData.take(10)}")
    return pixelData.toNSData()
}



@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ByteArray.bytesToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val uiImage = this.usePinned { pinned ->
        val data = NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
        UIImage.imageWithData(data)
    } ?: throw IllegalArgumentException("Could not decode ByteArray to UIImage")

    val scaledImage = uiImage.scaleTo(inputWidth, inputHeight)

    val pixelData = scaledImage.toRGBByteArray(normalize)

    return pixelData.toNSData()
}
