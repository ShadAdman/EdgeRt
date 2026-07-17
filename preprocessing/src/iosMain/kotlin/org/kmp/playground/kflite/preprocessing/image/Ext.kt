package org.kmp.playground.kflite.preprocessing.image

import kotlinx.cinterop.*
import platform.CoreGraphics.CGColorRenderingIntent.kCGRenderingIntentDefault
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGDataProviderCreateWithData
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageCreate
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGBitmapByteOrder32Big
import platform.CoreGraphics.CGContextDrawImage
import platform.CoreGraphics.CGBitmapContextCreate
import platform.Foundation.*
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
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
