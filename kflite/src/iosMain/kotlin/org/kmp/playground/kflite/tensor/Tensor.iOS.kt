package org.kmp.playground.kflite.tensor

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class Tensor(private val runtimeTensor: PlatformTensor) {
    actual val dataType: TensorDataType get() = TensorDataType.FLOAT32 // FIXME
    actual val name: String get() = "" // FIXME
    actual val shape: IntArray get() = IntArray(0) // FIXME
}



