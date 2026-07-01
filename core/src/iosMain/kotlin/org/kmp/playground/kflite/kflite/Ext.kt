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

fun ByteArray.writeToTempFile(): String {
    val tempDir = NSTemporaryDirectory()
    val fileName = "model_${currentTimeMillis()}.tflite"
    val filePath = tempDir + fileName
    val data = this.toNSData()
    data.writeToFile(filePath, true)
    return filePath
}

private fun currentTimeMillis(): Long = platform.Foundation.NSDate().timeIntervalSince1970.toLong() * 1000






