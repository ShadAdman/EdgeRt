package org.kmp.playground.kflite

sealed interface ModelSource

class ByteArraySource(
    val bytes: ByteArray
) : ModelSource

class FileSource(
    val path: String
) : ModelSource

object KFliteModel {
    fun fromBytes(bytes: ByteArray): ModelSource = ByteArraySource(bytes)
    fun fromFile(path: String): ModelSource = FileSource(path)
}
