package org.kmp.playground.kflite

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import cocoapods.TFLTensorFlowLite.TFLTensor
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
internal fun TFLTensor.toTensor() = Tensor(this)

internal fun TensorShape.getNSNumberDimensionList() = dimensions.map(::NSNumber)

internal fun ByteArray.writeToTempFile(prefix: String = "model", suffix: String = ".tflite"): String {
    val tempDir = NSTemporaryDirectory()
    val filePath = "$tempDir/$prefix$suffix"
    val nsData = this.toNSData()
    nsData.writeToFile(filePath, true)
    return filePath
}


@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData = memScoped {
    NSData.dataWithBytes(
        bytes = allocArray<ByteVar>(this@toNSData.size).also {
            this@toNSData.forEachIndexed { index, byte ->
                it[index] = byte
            }
        },
        length = this@toNSData.size.toULong()
    )
}


@OptIn(ExperimentalForeignApi::class)
internal fun ImageBitmap.toUIImage(): UIImage? {
    val skiaBitmap = this.asSkiaBitmap()
    val width = skiaBitmap.width
    val height = skiaBitmap.height

    val bytesPerPixel = 4
    val bitsPerComponent = 8
    val bytesPerRow = bytesPerPixel * width


    val pixelBytes: ByteArray? = skiaBitmap.readPixels(
        dstInfo = org.jetbrains.skia.ImageInfo.makeN32Premul(width, height),
        dstRowBytes = bytesPerRow
    )

    val imageSize = pixelBytes?.size ?: 0
    val buffer = nativeHeap.allocArray<ByteVar>(imageSize)
    for (i in 0 until imageSize) {
        buffer[i] = pixelBytes!![i]
    }

    val provider = CGDataProviderCreateWithData(
        info = null,
        data = buffer,
        size = imageSize.toULong(),
        releaseData = staticCFunction { _, data, _ -> nativeHeap.free(data!!) })

    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val bitmapInfo =
        kCGBitmapByteOrder32Big or CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value

    val cgImage = CGImageCreate(
        width.toULong(),
        height.toULong(),
        bitsPerComponent.toULong(),
        (bytesPerPixel * bitsPerComponent).toULong(),
        bytesPerRow.toULong(),
        colorSpace,
        bitmapInfo.toUInt(),
        provider,
        null,
        false,
        kCGRenderingIntentDefault,
    ) ?: return null

    return UIImage.imageWithCGImage(cgImage)
}



@OptIn(ExperimentalForeignApi::class)
fun UIImage.scaleTo(width: Int, height: Int): UIImage {
    val size = CGSizeMake(width.toDouble(), height.toDouble())

    UIGraphicsBeginImageContextWithOptions(size, false, 0.0)

    size.useContents {
        this@scaleTo.drawInRect(CGRectMake(0.0, 0.0, this.width, this.height))
    }

    val scaledImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return scaledImage ?: error("Failed to scale image")
}

const val kCGImageAlphaPremultipliedLast = 1

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
    rawData.usePinned { pinned ->
        val context = CGBitmapContextCreate(
            pinned.addressOf(0),
            width.toULong(),
            height.toULong(),
            8.toULong(),
            (bytesPerPixel * width).toULong(),
            colorSpace,
            kCGImageAlphaPremultipliedLast.toUInt()
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