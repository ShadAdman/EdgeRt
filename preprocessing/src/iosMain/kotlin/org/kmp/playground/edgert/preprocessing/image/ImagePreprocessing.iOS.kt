package org.kmp.playground.edgert.preprocessing.image

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.Foundation.create
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.kmp.playground.edgert.edgert.toNSData

actual fun ImageBitmap.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer {
    val config = ImageProcessorConfig().apply(block)
    val uiImage = this.toUIImage() ?: error("Failed to convert ImageBitmap to UIImage")
    val processedImage = uiImage.applyConfig(config)
    val pixelData = processedImage.toRGBByteArray(config.normalize)
    return pixelData.toNSData()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ByteArray.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer {
    val config = ImageProcessorConfig().apply(block)
    val uiImage = this.usePinned { pinned ->
        val data = NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
        UIImage.imageWithData(data)
    } ?: throw IllegalArgumentException("Could not decode ByteArray to UIImage")

    val processedImage = uiImage.applyConfig(config)
    val pixelData = processedImage.toRGBByteArray(config.normalize)
    return pixelData.toNSData()
}

actual fun ImageBitmap.imageToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    return preprocess(inputAllocateSize) {
        resize(inputWidth, inputHeight)
        this.normalize = normalize
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ByteArray.bytesToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    return preprocess(inputAllocateSize) {
        resize(inputWidth, inputHeight)
        this.normalize = normalize
    }
}
