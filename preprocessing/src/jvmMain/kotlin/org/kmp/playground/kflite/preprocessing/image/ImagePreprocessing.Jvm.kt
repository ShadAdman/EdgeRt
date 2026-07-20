package org.kmp.playground.kflite.preprocessing.image

import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

actual typealias PlatformImage = BufferedImage

actual fun PlatformImage.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer {
    val config = ImageProcessorConfig().apply(block)
    val processedImage = this.applyConfig(config)
    return processedImage.toTensorBuffer(allocateSize, config.normalize)
}

actual fun ByteArray.preprocess(
    allocateSize: Int,
    block: ImageProcessorConfig.() -> Unit
): TensorBuffer {
    val config = ImageProcessorConfig().apply(block)
    val inputStream = ByteArrayInputStream(this)
    val originalImage = ImageIO.read(inputStream) ?: throw IllegalArgumentException("Could not decode ByteArray to Image")
    val processedImage = originalImage.applyConfig(config)
    return processedImage.toTensorBuffer(allocateSize, config.normalize)
}

private fun BufferedImage.applyConfig(config: ImageProcessorConfig): BufferedImage {
    var result = this

    // 1. Rotate (Simplified, AWT rotation is complex, just doing 90/180/270 or skipping for now if not common)
    // For now let's just implement resize and crop
    
    // 2. Crop
    if (config.cropX != null && config.cropY != null && config.cropWidth != null && config.cropHeight != null) {
        result = result.getSubimage(config.cropX!!, config.cropY!!, config.cropWidth!!, config.cropHeight!!)
    }

    // 3. Resize
    if (config.resizeWidth != null && config.resizeHeight != null) {
        val scaledImage = result.getScaledInstance(config.resizeWidth!!, config.resizeHeight!!, Image.SCALE_SMOOTH)
        val bufferedScaledImage = BufferedImage(config.resizeWidth!!, config.resizeHeight!!, BufferedImage.TYPE_INT_RGB)
        val g2d: Graphics2D = bufferedScaledImage.createGraphics()
        g2d.drawImage(scaledImage, 0, 0, null)
        g2d.dispose()
        result = bufferedScaledImage
    }

    return result
}

private fun BufferedImage.toTensorBuffer(allocateSize: Int, normalize: Boolean): TensorBuffer {
    val width = this.width
    val height = this.height
    val byteBuffer = ByteBuffer.allocateDirect(allocateSize)
    byteBuffer.order(ByteOrder.nativeOrder())

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this.getRGB(x, y)

            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

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

actual fun PlatformImage.imageToScaledByteBuffer(
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
