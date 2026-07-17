package org.kmp.playground.kflite.preprocessing.image

import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.CoreGraphics.CGColorRenderingIntent.kCGRenderingIntentDefault
import platform.Foundation.*
import platform.UIKit.*
import kotlin.math.PI
import kotlin.math.roundToInt

@OptIn(ExperimentalForeignApi::class)
fun UIImage.scaleTo(width: Int, height: Int): UIImage {
    val size = CGSizeMake(width.toDouble(), height.toDouble())

    UIGraphicsBeginImageContextWithOptions(size, false, 0.0)

    size.useContents {
        this@scaleTo.drawInRect(CGRectMake(0.0, 0.0, this.width, this.height))
    }

    val scaledImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    scaledImage?.size?.useContents {
        println("Original scaled UIImage size: ${this.width}x${this.height}")
    }
    return scaledImage ?: error("Failed to scale image")
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.rotate(degrees: Float): UIImage {
    val radians = degrees * PI / 180.0
    val size = this.size.useContents { CGSizeMake(width, height) }
    UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
    val context = UIGraphicsGetCurrentContext()

    size.useContents {
        CGContextTranslateCTM(context, width / 2.0, height / 2.0)
        CGContextRotateCTM(context, radians)
        this@rotate.drawInRect(CGRectMake(-width / 2.0, -height / 2.0, width, height))
    }

    val rotatedImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return rotatedImage ?: this
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.crop(x: Int, y: Int, width: Int, height: Int): UIImage {
    val rect = CGRectMake(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
    val cgImage = this.CGImage()?.let {
        CGImageCreateWithImageInRect(it, rect)
    }
    return cgImage?.let { UIImage.imageWithCGImage(it) } ?: this
}

internal fun UIImage.applyConfig(config: ImageProcessorConfig): UIImage {
    var result = this
    // 1. Rotate
    config.rotationDegrees?.let {
        result = result.rotate(it)
    }
    // 2. Crop
    if (config.cropX != null && config.cropY != null && config.cropWidth != null && config.cropHeight != null) {
        result = result.crop(config.cropX!!, config.cropY!!, config.cropWidth!!, config.cropHeight!!)
    }
    // 3. Resize
    if (config.resizeWidth != null && config.resizeHeight != null) {
        result = result.scaleTo(config.resizeWidth!!, config.resizeHeight!!)
    }
    return result
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toRGBByteArray(normalize: Boolean): ByteArray {
    var width = 0
    var height = 0
    this.size.useContents {
        width = this.width.roundToInt()
        height = this.height.roundToInt()
    }

    val bytesPerPixel = 4
    val byteCount = width * height * bytesPerPixel

    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val rawData = ByteArray(byteCount)
    val bitmapInfo =
        kCGBitmapByteOrder32Big or CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
    rawData.usePinned { pinned ->
        val context = CGBitmapContextCreate(
            pinned.addressOf(0),
            width.toULong(),
            height.toULong(),
            8.toULong(),
            (bytesPerPixel * width).toULong(),
            colorSpace,
            bitmapInfo
        ) ?: error("Could not create context")

        CGContextDrawImage(context, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), this.CGImage())

        val result = mutableListOf<Byte>()
        for (i in 0 until byteCount step bytesPerPixel) {
            val r = rawData[i].toUByte().toInt()
            val g = rawData[i + 1].toUByte().toInt()
            val b = rawData[i + 2].toUByte().toInt()
            if (normalize) {
                result.addAll(listOf(
                    (r / 255.0f).toBits().toByteList(),
                    (g / 255.0f).toBits().toByteList(),
                    (b / 255.0f).toBits().toByteList()
                ).flatten())
            } else {
                result.add(r.toByte())
                result.add(g.toByte())
                result.add(b.toByte())
            }
        }
        return result.toByteArray()
    }
}

fun Int.toByteList(): List<Byte> = listOf(
    (this and 0xFF).toByte(),
    ((this shr 8) and 0xFF).toByte(),
    ((this shr 16) and 0xFF).toByte(),
    ((this shr 24) and 0xFF).toByte()
)
