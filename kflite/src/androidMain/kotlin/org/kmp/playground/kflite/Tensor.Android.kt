package org.kmp.playground.kflite

import org.tensorflow.lite.DataType as TFLiteDataType
import com.google.ai.edge.litert.TensorBuffer

actual class Tensor(
    internal val platformTensor: Any
) {
    actual val dataType: TensorDataType
        get() = when (platformTensor) {
            is org.tensorflow.lite.Tensor -> platformTensor.dataType().toTensorDataType()
            is TensorBuffer -> TensorDataType.FLOAT32 // FIXME: need to get from buffer
            else -> throw IllegalArgumentException("Unknown tensor type: ${platformTensor::class.java.name}")
        }
    actual val name: String
        get() = when (platformTensor) {
            is org.tensorflow.lite.Tensor -> platformTensor.name()
            is TensorBuffer -> "LiteRT Tensor"
            else -> throw IllegalArgumentException("Unknown tensor type: ${platformTensor::class.java.name}")
        }
    actual val shape: IntArray
        get() = when (platformTensor) {
            is org.tensorflow.lite.Tensor -> platformTensor.shape()
            is TensorBuffer -> platformTensor.readInt()
            else -> throw IllegalArgumentException("Unknown tensor type: ${platformTensor::class.java.name}")
        }
}

private fun TFLiteDataType.toTensorDataType() = when (this) {
    TFLiteDataType.FLOAT32 -> TensorDataType.FLOAT32
    TFLiteDataType.INT32 -> TensorDataType.INT32
    TFLiteDataType.UINT8 -> TensorDataType.UINT8
    TFLiteDataType.INT64 -> TensorDataType.INT64
    else -> throw IllegalArgumentException("TFLite data type $this not supported in MPP.")
}
