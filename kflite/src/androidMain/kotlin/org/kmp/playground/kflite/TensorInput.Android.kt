package org.kmp.playground.kflite

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.scale
import androidx.core.graphics.get
import android.graphics.Color



actual fun ImageBitmap.toScaledByteBuffer(
    inputWidth: Int,
    inputHeight: Int,
    inputAllocateSize: Int,
    normalize: Boolean
): TensorBuffer {
    val bitmap = this.asAndroidBitmap().scale(inputWidth, inputHeight)
    val byteBuffer = ByteBuffer.allocateDirect(inputAllocateSize)
    byteBuffer.order(ByteOrder.nativeOrder())

    for (y in 0 until inputHeight) {
        for (x in 0 until inputWidth) {
            val pixel = bitmap[x, y]

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