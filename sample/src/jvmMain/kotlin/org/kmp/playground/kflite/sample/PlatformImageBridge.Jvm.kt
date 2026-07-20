package org.kmp.playground.kflite.sample

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import org.kmp.playground.kflite.preprocessing.image.PlatformImage
import java.awt.image.BufferedImage

actual fun ImageBitmap.toPlatformImage(): PlatformImage = this.toAwtImage() as BufferedImage
