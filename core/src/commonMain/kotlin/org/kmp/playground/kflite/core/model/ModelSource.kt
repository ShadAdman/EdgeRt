package org.kmp.playground.kflite.core.model

sealed interface ModelSource

class ByteArraySource(
    val bytes: ByteArray
) : ModelSource

class FileSource(
    val path: String
) : ModelSource
