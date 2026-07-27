package org.kmp.playground.edgert.preprocessing.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

actual fun ImageBitmap.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer {
    val config = ImageProcessorConfig().apply(block)
    val processedBitmap = this.asAndroidBitmap().applyConfig(config)
    return processedBitmap.toTensorBuffer(allocateSize, config.normalize)
}

actual fun ByteArray.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer {
    val config = ImageProcessorConfig().apply(block)
    val originalBitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        ?: throw IllegalArgumentException("Could not decode ByteArray to Bitmap")
    val processedBitmap = originalBitmap.applyConfig(config)
    return processedBitmap.toTensorBuffer(allocateSize, config.normalize)
}

private fun Bitmap.applyConfig(config: ImageProcessorConfig): Bitmap {
    var result = this

    // 1. Rotate
    config.rotationDegrees?.let { degrees ->
        val matrix = Matrix().apply { postRotate(degrees) }
        result = Bitmap.createBitmap(result, 0, 0, result.width, result.height, matrix, true)
    }

    // 2. Crop
    if (config.cropX != null && config.cropY != null && config.cropWidth != null && config.cropHeight != null) {
        result = Bitmap.createBitmap(
            result,
            config.cropX!!,
            config.cropY!!,
            config.cropWidth!!,
            config.cropHeight!!
        )
    }

    // 3. Resize
    if (config.resizeWidth != null && config.resizeHeight != null) {
        result = Bitmap.createScaledBitmap(result, config.resizeWidth!!, config.resizeHeight!!, true)
    }

    return result
}

private fun Bitmap.toTensorBuffer(allocateSize: Int, normalize: Boolean): TensorBuffer {
    val width = this.width
    val height = this.height
    val byteBuffer = ByteBuffer.allocateDirect(allocateSize)
    byteBuffer.order(ByteOrder.nativeOrder())

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this.getPixel(x, y)

            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            if (normalize) {
                byteBuffer.putFloat(r / 255.0f)
                byteBuffer.putFloat(g / 255.0f)
                byteBuffer.putFloat(b / 255.0f)
            } else {
                byteBuffer.put(r.toByte())
                byteBuffer.put(g.toByte())
                byteBuffer.put(b.toByte())
            }
        }
    }

    return byteBuffer
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
