package org.kmp.playground.kflite.runtime.litert.native

import kotlinx.cinterop.*
import org.kmp.playground.kflite.native.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

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
            Tensor(NativeLiteRTTensor(index, tensorPtr.value, true))
        }
    }

    fun getOutputTensor(index: Int): Tensor {
        val signature = getSignature() ?: error("No signature")
        return memScoped {
            val tensorPtr = alloc<LiteRtTensorVar>()
            LiteRtGetSignatureOutputTensorByIndex(signature, index.toULong(), tensorPtr.ptr).checkStatus()
            Tensor(NativeLiteRTTensor(index, tensorPtr.value, false))
        }
    }

    fun resizeInput(index: Int, shape: IntArray) {
        // LiteRT resizing might be different
    }

    fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
        // Implement running
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
            LiteRtGetTensorTypeId(handle, typeId.ptr).checkStatus()
            // Map typeId to TensorDataType
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
                IntArray(rankedType.rank.toInt()) { i -> rankedType.layout.dimensions!![i].toInt() }
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
