package org.kmp.playground.kflite.model

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

sealed interface ModelSource

class ByteArraySource(
    val bytes: ByteArray
) : ModelSource

class FileSource(
    val path: String
) : ModelSource

class AssetSource(
    val path: String
) : ModelSource

class ResourceSource(
    val path: String
) : ModelSource






