package org.kmp.playground.kflite.preprocessing.image

typealias TensorBuffer = Any

/**
 * Platform-specific image type.
 * On Android: android.graphics.Bitmap
 * On iOS: platform.UIKit.UIImage
 */
expect class PlatformImage

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
