package org.kmp.playground.kflite.sample

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import org.kmp.playground.kflite.preprocessing.image.PlatformImage

actual fun ImageBitmap.toPlatformImage(): PlatformImage = this.asAndroidBitmap()
