package org.kmp.playground.kflite

import cocoapods.TFLTensorFlowLite.TFLTensorDataType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.memScoped
import platform.Foundation.NSNumber

actual class Tensor @OptIn(ExperimentalForeignApi::class) constructor(
    internal val platformTensor: PlatformTensor
) {
    @OptIn(ExperimentalForeignApi::class)
    actual val dataType: TensorDataType
        get() = platformTensor.dataType.toTensorDataType()

    @OptIn(ExperimentalForeignApi::class)
    actual val name: String
        get() = platformTensor.name()

    @OptIn(ExperimentalForeignApi::class)
    actual val shape: IntArray
        get() {
            @Suppress("UNCHECKED_CAST")
            val rawShapeArr = errorHandled { errPtr ->
                platformTensor.shapeWithError(errPtr)
            } as List<NSNumber>

            return rawShapeArr.map {
                it.unsignedIntValue().toInt()
            }.toIntArray()
        }
}


@OptIn(ExperimentalForeignApi::class)
fun TFLTensorDataType.toTensorDataType() = when (this) {
    TFLTensorDataType.TFLTensorDataTypeFloat32 -> TensorDataType.FLOAT32
    TFLTensorDataType.TFLTensorDataTypeInt32 -> TensorDataType.INT32
    TFLTensorDataType.TFLTensorDataTypeUInt8 -> TensorDataType.UINT8
    TFLTensorDataType.TFLTensorDataTypeInt64 -> TensorDataType.INT64

    TFLTensorDataType.TFLTensorDataTypeFloat16 ->
        throw IllegalArgumentException("TFLTensorDataTypeFloat16 not supported.")
    TFLTensorDataType.TFLTensorDataTypeBool ->
        throw IllegalArgumentException("TFLTensorDataTypeBool not supported.")
    TFLTensorDataType.TFLTensorDataTypeInt16 ->
        throw IllegalArgumentException("TFLTensorDataTypeInt16 not supported.")
    TFLTensorDataType.TFLTensorDataTypeInt8 ->
        throw IllegalArgumentException("TFLTensorDataTypeInt8 not supported.")
    TFLTensorDataType.TFLTensorDataTypeNoType ->
        throw IllegalArgumentException("TFLTensorDataTypeNoType: wrong tensor type.")

    else -> throw IllegalArgumentException("TFLTensorDataTypeNoType: wrong tensor type.")
}
