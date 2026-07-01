package org.kmp.playground.kflite.tensor

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

actual class Tensor(private val runtimeTensor: RuntimeTensor) {
    actual val dataType: TensorDataType get() = runtimeTensor.dataType
    actual val name: String get() = runtimeTensor.name
    actual val shape: IntArray get() = runtimeTensor.shape
}



