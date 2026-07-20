package org.kmp.playground.kflite.preprocessing.image

typealias TensorBuffer = Any

/**
 * Platform-specific image type.
 * On Android: android.graphics.Bitmap
 * On iOS: platform.UIKit.UIImage
 */
expect class PlatformImage

class ImageProcessorConfig {
    var resizeWidth: Int? = null
    var resizeHeight: Int? = null
    var cropX: Int? = null
    var cropY: Int? = null
    var cropWidth: Int? = null
    var cropHeight: Int? = null
    var rotationDegrees: Float? = null
    var normalize: Boolean = false

    fun resize(width: Int, height: Int) {
        resizeWidth = width
        resizeHeight = height
    }

    fun crop(x: Int, y: Int, width: Int, height: Int) {
        cropX = x
        cropY = y
        cropWidth = width
        cropHeight = height
    }

    fun rotate(degrees: Float) {
        rotationDegrees = degrees
    }
}

expect fun PlatformImage.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer

expect fun ByteArray.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer

expect fun PlatformImage.imageToScaledByteBuffer(
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
