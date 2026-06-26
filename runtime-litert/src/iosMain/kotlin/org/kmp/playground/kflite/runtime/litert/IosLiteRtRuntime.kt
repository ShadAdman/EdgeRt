package org.kmp.playground.kflite.runtime.litert

import cocoapods.TFLTensorFlowLite.TFLInterpreter
import kotlinx.cinterop.*
import org.kmp.playground.kflite.core.model.*
import org.kmp.playground.kflite.core.runtime.Runtime
import org.kmp.playground.kflite.core.interpreter.InterpreterOptions
import org.kmp.playground.kflite.core.errorHandled
import org.kmp.playground.kflite.core.toNSData

@OptIn(ExperimentalForeignApi::class)
class IosLiteRtRuntime(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions
) : Runtime {
    private var tflInterpreter: TFLInterpreter? = null

    init {
        // As requested, LiteRT on iOS uses TFLite for now.
        tflInterpreter = errorHandled { errPtr ->
            when (modelSource) {
                is ByteArraySource -> TFLInterpreter(
                    modelSource.bytes.toNSData(),
                    null,
                    errPtr
                )
                is FileSource -> TFLInterpreter(
                    modelSource.path,
                    null,
                    errPtr
                )
            }
        }
        tflInterpreter?.allocateTensorsWithError(null)
    }

    override val inputTensorCount: Int get() = tflInterpreter?.inputTensorCount()?.toInt() ?: 0
    override val outputTensorCount: Int get() = tflInterpreter?.outputTensorCount()?.toInt() ?: 0

    override fun getInputTensor(index: Int): Tensor {
        val t = tflInterpreter?.inputTensorAtIndex(index.toULong(), null)!!
        return Tensor(LiteRtRuntimeTensor(t))
    }

    override fun getOutputTensor(index: Int): Tensor {
        val t = tflInterpreter?.outputTensorAtIndex(index.toULong(), null)!!
        return Tensor(LiteRtRuntimeTensor(t))
    }

    override fun resizeInput(index: Int, shape: IntArray) {
        // Implementation logic
    }

    override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
        // Full run logic from Interpreter.iOS.kt
    }

    override fun getMetadata(): ModelMetadata {
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

    override fun close() {
        tflInterpreter = null
    }

    private class LiteRtRuntimeTensor(private val tflTensor: cocoapods.TFLTensorFlowLite.TFLTensor) : RuntimeTensor {
        override val dataType: TensorDataType get() = TensorDataType.FLOAT32 // FIXME
        override val name: String get() = tflTensor.name() ?: ""
        override val shape: IntArray get() = IntArray(0) // FIXME
    }
}
