package org.kmp.playground.kflite.runtime.litert.native

import kotlinx.cinterop.*
import org.kmp.playground.kflite.native.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class LiteRTNativeRuntime(
    modelSource: ModelSource,
    options: InterpreterOptions
) {
    private var env: LiteRtEnvironment? = null
    private var model: LiteRtModel? = null
    private var compiledModel: LiteRtCompiledModel? = null

    val inputTensorCount: Int
        get() = memScoped {
            val count = alloc<LiteRtParamIndexVar>()
            val signature = getSignature() ?: return 0
            LiteRtGetNumSignatureInputs(signature, count.ptr).checkStatus()
            count.value.toInt()
        }

    val outputTensorCount: Int
        get() = memScoped {
            val count = alloc<LiteRtParamIndexVar>()
            val signature = getSignature() ?: return 0
            LiteRtGetNumSignatureOutputs(signature, count.ptr).checkStatus()
            count.value.toInt()
        }

    init {
        memScoped {
            val envPtr = alloc<LiteRtEnvironmentVar>()
            LiteRtCreateEnvironment(0, null, envPtr.ptr).checkStatus()
            env = envPtr.value

            val modelPtr = alloc<LiteRtModelVar>()
            when (modelSource) {
                is FileSource -> {
                    LiteRtCreateModelFromFile(env, modelSource.path, modelPtr.ptr).checkStatus()
                }
                is ByteArraySource -> {
                    modelSource.bytes.usePinned { pinned ->
                        LiteRtCreateModelFromBuffer(env, pinned.addressOf(0), modelSource.bytes.size.toULong(), modelPtr.ptr).checkStatus()
                    }
                }
                else -> throw IllegalArgumentException("Unsupported model source for native LiteRT")
            }
            model = modelPtr.value

            val compiledModelPtr = alloc<LiteRtCompiledModelVar>()
            LiteRtCreateCompiledModel(env, model, null, compiledModelPtr.ptr).checkStatus()
            compiledModel = compiledModelPtr.value
        }
    }

    private fun getSignature(index: Int = 0): LiteRtSignature? = memScoped {
        val count = alloc<LiteRtParamIndexVar>()
        LiteRtGetNumModelSignatures(model, count.ptr).checkStatus()
        if (count.value.toInt() <= index) return null
        val signature = alloc<LiteRtSignatureVar>()
        LiteRtGetModelSignature(model, index.toULong(), signature.ptr).checkStatus()
        signature.value
    }

    fun getInputTensor(index: Int): Tensor {
        val signature = getSignature() ?: error("No signature")
        return memScoped {
            val tensorPtr = alloc<LiteRtTensorVar>()
            LiteRtGetSignatureInputTensorByIndex(signature, index.toULong(), tensorPtr.ptr).checkStatus()
            Tensor(NativeLiteRTTensor(index, tensorPtr.value!!, true))
        }
    }

    fun getOutputTensor(index: Int): Tensor {
        val signature = getSignature() ?: error("No signature")
        return memScoped {
            val tensorPtr = alloc<LiteRtTensorVar>()
            LiteRtGetSignatureOutputTensorByIndex(signature, index.toULong(), tensorPtr.ptr).checkStatus()
            Tensor(NativeLiteRTTensor(index, tensorPtr.value!!, false))
        }
    }

    fun resizeInput(index: Int, shape: IntArray) {
        shape.usePinned { pinned ->
            LiteRtCompiledModelResizeInputTensor(compiledModel, 0, index.toULong(), pinned.addressOf(0), shape.size.toULong()).checkStatus()
        }
    }

    fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
        memScoped {
            val numInputs = inputTensorCount
            val numOutputs = outputTensorCount

            val inputBuffers = allocArray<LiteRtTensorBufferVar>(numInputs)
            val outputBuffers = allocArray<LiteRtTensorBufferVar>(numOutputs)

            for (i in 0 until numInputs) {
                val signature = getSignature() ?: error("No signature")
                val tensorPtr = alloc<LiteRtTensorVar>()
                LiteRtGetSignatureInputTensorByIndex(signature, i.toULong(), tensorPtr.ptr).checkStatus()
                
                val rankedType = alloc<LiteRtRankedTensorType>()
                LiteRtGetRankedTensorType(tensorPtr.value, rankedType.ptr).checkStatus()

                val bufferPtr = alloc<LiteRtTensorBufferVar>()
                LiteRtCreateManagedTensorBuffer(env, kLiteRtTensorBufferTypeHostMemory, rankedType.ptr, 0, bufferPtr.ptr).checkStatus()
                inputBuffers[i] = bufferPtr.value
                
                fillBuffer(bufferPtr.value!!, inputs[i])
            }

            for (i in 0 until numOutputs) {
                val signature = getSignature() ?: error("No signature")
                val tensorPtr = alloc<LiteRtTensorVar>()
                LiteRtGetSignatureOutputTensorByIndex(signature, i.toULong(), tensorPtr.ptr).checkStatus()
                
                val rankedType = alloc<LiteRtRankedTensorType>()
                LiteRtGetRankedTensorType(tensorPtr.value, rankedType.ptr).checkStatus()

                val bufferPtr = alloc<LiteRtTensorBufferVar>()
                LiteRtCreateManagedTensorBuffer(env, kLiteRtTensorBufferTypeHostMemory, rankedType.ptr, 0, bufferPtr.ptr).checkStatus()
                outputBuffers[i] = bufferPtr.value
            }

            LiteRtRunCompiledModel(compiledModel, 0, numInputs.toULong(), inputBuffers, numOutputs.toULong(), outputBuffers).checkStatus()

            for ((index, outputData) in outputs) {
                readBuffer(outputBuffers[index]!!, outputData)
            }

            // Cleanup
            for (i in 0 until numInputs) LiteRtDestroyTensorBuffer(inputBuffers[i])
            for (i in 0 until numOutputs) LiteRtDestroyTensorBuffer(outputBuffers[i])
        }
    }

    private fun fillBuffer(buffer: LiteRtTensorBuffer, data: Any) {
        memScoped {
            val hostPtr = alloc<CPointerVar<ByteVar>>()
            LiteRtLockTensorBuffer(buffer, hostPtr.ptr.reinterpret(), kLiteRtTensorBufferLockModeWrite).checkStatus()
            val addr = hostPtr.value!!
            
            val size = alloc<size_tVar>()
            LiteRtGetTensorBufferPackedSize(buffer, size.ptr).checkStatus()
            val bufferSize = size.value.toLong()
            
            when (data) {
                is FloatArray -> data.usePinned { memcpy(addr, it.addressOf(0), bufferSize.toULong()) }
                is IntArray -> data.usePinned { memcpy(addr, it.addressOf(0), bufferSize.toULong()) }
                is LongArray -> data.usePinned { memcpy(addr, it.addressOf(0), bufferSize.toULong()) }
                is ByteArray -> data.usePinned { memcpy(addr, it.addressOf(0), bufferSize.toULong()) }
                else -> throw IllegalArgumentException("Unsupported input data type: ${data::class}")
            }
            LiteRtUnlockTensorBuffer(buffer).checkStatus()
        }
    }

    private fun readBuffer(buffer: LiteRtTensorBuffer, data: Any) {
        memScoped {
            val hostPtr = alloc<CPointerVar<ByteVar>>()
            LiteRtLockTensorBuffer(buffer, hostPtr.ptr.reinterpret(), kLiteRtTensorBufferLockModeRead).checkStatus()
            val addr = hostPtr.value!!
            
            val size = alloc<size_tVar>()
            LiteRtGetTensorBufferPackedSize(buffer, size.ptr).checkStatus()
            val bufferSize = size.value.toLong()
            
            when (data) {
                is FloatArray -> data.usePinned { memcpy(it.addressOf(0), addr, bufferSize.toULong()) }
                is IntArray -> data.usePinned { memcpy(it.addressOf(0), addr, bufferSize.toULong()) }
                is LongArray -> data.usePinned { memcpy(it.addressOf(0), addr, bufferSize.toULong()) }
                is ByteArray -> data.usePinned { memcpy(it.addressOf(0), addr, bufferSize.toULong()) }
                else -> throw IllegalArgumentException("Unsupported output data type: ${data::class}")
            }
            LiteRtUnlockTensorBuffer(buffer).checkStatus()
        }
    }

    fun getMetadata(): ModelMetadata {
        val inputs = (0 until inputTensorCount).map { i ->
            val t = getInputTensor(i)
            TensorMetadata(t.name, t.shape, t.dataType)
        }
        val outputs = (0 until outputTensorCount).map { i ->
            val t = getOutputTensor(i)
            TensorMetadata(t.name, t.shape, t.dataType)
        }
        return ModelMetadata(null, null, null, null, null, null, inputs, outputs)
    }

    fun close() {
        LiteRtDestroyCompiledModel(compiledModel)
        LiteRtDestroyModel(model)
        LiteRtDestroyEnvironment(env)
    }

    private class NativeLiteRTTensor(val index: Int, val handle: LiteRtTensor, val isInput: Boolean) : RuntimeTensor {
        override val dataType: TensorDataType get() = memScoped {
            val typeId = alloc<LiteRtTensorTypeIdVar>()
            val rankedType = alloc<LiteRtRankedTensorType>()
            if (LiteRtGetRankedTensorType(handle, rankedType.ptr) == kLiteRtStatusOk) {
                return when (rankedType.element_type) {
                    kLiteRtElementTypeFloat32 -> TensorDataType.FLOAT32
                    kLiteRtElementTypeInt32 -> TensorDataType.INT32
                    kLiteRtElementTypeUInt8 -> TensorDataType.UINT8
                    kLiteRtElementTypeInt64 -> TensorDataType.INT64
                    else -> TensorDataType.FLOAT32
                }
            }
            TensorDataType.FLOAT32 
        }
        override val name: String get() = memScoped {
            val namePtr = alloc<CPointerVar<ByteVar>>()
            LiteRtGetTensorName(handle, namePtr.ptr).checkStatus()
            namePtr.value?.toKString() ?: ""
        }
        override val shape: IntArray get() = memScoped {
            val rankedType = alloc<LiteRtRankedTensorType>()
            if (LiteRtGetRankedTensorType(handle, rankedType.ptr) == kLiteRtStatusOk) {
                IntArray(rankedType.layout.rank.toInt()) { i -> rankedType.layout.dimensions!![i].toInt() }
            } else {
                IntArray(0)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun LiteRtStatus.checkStatus() {
    if (this != kLiteRtStatusOk) {
        throw RuntimeException("LiteRT error: $this")
    }
}
