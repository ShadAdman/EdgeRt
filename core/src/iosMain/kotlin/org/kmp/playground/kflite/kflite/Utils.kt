package org.kmp.playground.kflite.kflite

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

import kotlinx.cinterop.*
import platform.Foundation.NSErrorVar

@OptIn(ExperimentalForeignApi::class)
fun <T> errorHandled(block: (CPointer<CPointerVar<NSErrorVar>>?) -> T): T {
    memScoped {
        val errorPtr = alloc<CPointerVar<NSErrorVar>>()
        val result = block(errorPtr.ptr)
        val error = errorPtr.value
        if (error != null) {
            throw Exception(error.localizedDescription)
        }
        return result
    }
}



