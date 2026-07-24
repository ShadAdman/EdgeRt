package org.kmp.playground.edgert.tensor

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class Tensor(private val runtimeTensor: PlatformTensor) {
    actual val dataType: TensorDataType get() = TensorDataType.FLOAT32 // FIXME
    actual val name: String get() = "" // FIXME
    actual val shape: IntArray get() = IntArray(0) // FIXME
}





