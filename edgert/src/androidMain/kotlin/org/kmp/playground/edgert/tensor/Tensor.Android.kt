package org.kmp.playground.edgert.tensor

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

actual class Tensor(private val runtimeTensor: RuntimeTensor) {
    actual val dataType: TensorDataType get() = runtimeTensor.dataType
    actual val name: String get() = runtimeTensor.name
    actual val shape: IntArray get() = runtimeTensor.shape
}





