package org.kmp.playground.kflite.tensor

actual class Tensor actual constructor(private val runtimeTensor: RuntimeTensor) {
    actual val dataType: TensorDataType get() = runtimeTensor.dataType
    actual val name: String get() = runtimeTensor.name
    actual val shape: IntArray get() = runtimeTensor.shape
}
