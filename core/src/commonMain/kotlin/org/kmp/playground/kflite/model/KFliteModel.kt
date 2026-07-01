package org.kmp.playground.kflite.model

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

object KFliteModel {
    fun fromBytes(bytes: ByteArray): ModelSource = ByteArraySource(bytes)
    fun fromFile(path: String): ModelSource = FileSource(path)
    fun fromAsset(path: String): ModelSource = AssetSource(path)
    fun fromResource(path: String): ModelSource = ResourceSource(path)
}






