package org.kmp.playground.kflite

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("TooGenericExceptionThrown")
internal fun <T> errorHandled(block: (CPointer<ObjCObjectVar<NSError?>>) -> T?): T? {
    val (result, error) = memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        runCatching {
            block(errorPtr.ptr)
        }.getOrNull() to errorPtr.value
    }
    if (error != null) throw Exception(error.description)
    return result
}
