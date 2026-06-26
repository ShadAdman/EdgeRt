package org.kmp.playground.kflite.core.model

actual class Tensor(internal val runtimeTensor: RuntimeTensor) {
    actual val dataType: TensorDataType get() = runtimeTensor.dataType
    actual val name: String get() = runtimeTensor.name
    actual val shape: IntArray get() = runtimeTensor.shape
}
