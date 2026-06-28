package org.kmp.playground.kflite.kflite

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToURL
import platform.Foundation.NSURL
import platform.Foundation.URLWithString

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






