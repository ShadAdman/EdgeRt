package org.kmp.playground.kflite.core.model

object KFliteModel {
    fun fromBytes(bytes: ByteArray): ModelSource = ByteArraySource(bytes)
    fun fromFile(path: String): ModelSource = FileSource(path)
}
