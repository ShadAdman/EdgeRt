package org.kmp.playground.edgert.model

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

object EdgeRtModel {
    fun fromBytes(bytes: ByteArray): ModelSource = ByteArraySource(bytes)
    fun fromFile(path: String): ModelSource = FileSource(path)
    fun fromAsset(path: String): ModelSource = AssetSource(path)
    fun fromResource(path: String): ModelSource = ResourceSource(path)
}








