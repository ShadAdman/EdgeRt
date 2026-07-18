package org.kmp.playground.kflite.preprocessing.image

import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.imageio.ImageIO

actual typealias PlatformImage = BufferedImage

actual fun PlatformImage.imageToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val scaledImage = this.getScaledInstance(inputWidth, inputHeight, Image.SCALE_SMOOTH)
    val bufferedScaledImage = BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_RGB)
    val g2d: Graphics2D = bufferedScaledImage.createGraphics()
    g2d.drawImage(scaledImage, 0, 0, null)
    g2d.dispose()

    val byteBuffer = ByteBuffer.allocateDirect(inputAllocateSize)
    byteBuffer.order(ByteOrder.nativeOrder())

    for (y in 0 until inputHeight) {
        for (x in 0 until inputWidth) {
            val pixel = bufferedScaledImage.getRGB(x, y)

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

actual fun ByteArray.bytesToScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val inputStream = ByteArrayInputStream(this)
    val originalImage = ImageIO.read(inputStream) ?: throw IllegalArgumentException("Could not decode ByteArray to Image")
    return originalImage.imageToScaledByteBuffer(inputWidth, inputHeight, inputAllocateSize, normalize)
}
