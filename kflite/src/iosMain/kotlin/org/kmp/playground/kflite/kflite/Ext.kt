package org.kmp.playground.kflite.kflite

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun FloatArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = (this.size * 4).toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun IntArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = (this.size * 4).toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun LongArray.toNSData(): NSData = usePinned {
    NSData.create(bytes = it.addressOf(0), length = (this.size * 8).toULong())
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toFloatArray(): FloatArray {
    val size = this.length.toInt() / 4
    val array = FloatArray(size)
    array.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return array
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toIntArray(): IntArray {
    val size = this.length.toInt() / 4
    val array = IntArray(size)
    array.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return array
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val array = ByteArray(size)
    array.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return array
}

fun ByteArray.writeToTempFile(): String {
    val tempDir = NSTemporaryDirectory()
    val fileName = "model_${currentTimeMillis()}.tflite"
    val filePath = tempDir + fileName
    val data = this.toNSData()
    data.writeToFile(filePath, true)
    return filePath
}

private fun currentTimeMillis(): Long = platform.Foundation.NSDate().timeIntervalSince1970.toLong() * 1000






