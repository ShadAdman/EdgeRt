package org.kmp.playground.edgert.model

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

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








