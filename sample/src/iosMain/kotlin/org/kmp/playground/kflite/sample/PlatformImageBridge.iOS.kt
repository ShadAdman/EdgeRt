package org.kmp.playground.kflite.sample

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.kmp.playground.kflite.preprocessing.image.PlatformImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class)
actual fun ImageBitmap.toPlatformImage(): PlatformImage {
    val skiaBitmap = this.asSkiaBitmap()
    val skiaImage = Image.makeFromBitmap(skiaBitmap)
    
    val encodedData = skiaImage.encodeToData(EncodedImageFormat.PNG) 
        ?: throw IllegalArgumentException("Could not encode ImageBitmap to PNG")
    val bytes = encodedData.bytes
    
    return bytes.usePinned { pinned ->
        val nsData = NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        UIImage.imageWithData(nsData) ?: throw IllegalArgumentException("Could not create UIImage from data")
    }
}
