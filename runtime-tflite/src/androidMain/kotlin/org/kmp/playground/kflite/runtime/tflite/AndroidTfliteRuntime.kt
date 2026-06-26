package org.kmp.playground.kflite.runtime.tflite

import android.content.Context
import org.kmp.playground.kflite.core.model.*
import org.kmp.playground.kflite.core.runtime.Runtime
import org.kmp.playground.kflite.core.interpreter.InterpreterOptions
import org.kmp.playground.kflite.core.writeToTempFile
import org.tensorflow.lite.DataType as TFLiteDataType
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel

class AndroidTfliteRuntime(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions,
    private val context: Context
) : Runtime {
    private val interpreter: org.tensorflow.lite.Interpreter by lazy {
        when (modelSource) {
            is ByteArraySource -> org.tensorflow.lite.Interpreter(
                modelSource.bytes.writeToTempFile(context),
                org.tensorflow.lite.Interpreter.Options().setNumThreads(options.numThreads)
            )
            is FileSource -> {
                val file = File(modelSource.path)
                val inputStream = FileInputStream(file)
                val modelBuffer = inputStream.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    file.length()
                )
                org.tensorflow.lite.Interpreter(modelBuffer, org.tensorflow.lite.Interpreter.Options().setNumThreads(options.numThreads))
            }
        }
    }

    override val inputTensorCount: Int get() = interpreter.inputTensorCount
    override val outputTensorCount: Int get() = interpreter.outputTensorCount

    override fun getInputTensor(index: Int): Tensor {
        return Tensor(TfliteRuntimeTensor(interpreter.getInputTensor(index)))
    }

    override fun getOutputTensor(index: Int): Tensor {
        return Tensor(TfliteRuntimeTensor(interpreter.getOutputTensor(index)))
    }

    override fun resizeInput(index: Int, shape: IntArray) {
        interpreter.resizeInput(index, shape)
    }

    override fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
        interpreter.runForMultipleInputsOutputs(inputs.toTypedArray(), outputs)
    }

    override fun getMetadata(): ModelMetadata {
        val inputs = (0 until inputTensorCount).map { i ->
            val t = interpreter.getInputTensor(i)
            TensorMetadata(t.name(), t.shape(), t.dataType().toCoreTensorDataType())
        }
        val outputs = (0 until outputTensorCount).map { i ->
            val t = interpreter.getOutputTensor(i)
            TensorMetadata(t.name(), t.shape(), t.dataType().toCoreTensorDataType())
        }
        return ModelMetadata(null, null, null, null, null, null, inputs, outputs)
    }

    override fun close() {
        interpreter.close()
    }

    private class TfliteRuntimeTensor(private val tflTensor: org.tensorflow.lite.Tensor) : RuntimeTensor {
        override val dataType: TensorDataType get() = tflTensor.dataType().toCoreTensorDataType()
        override val name: String get() = tflTensor.name()
        override val shape: IntArray get() = tflTensor.shape()
    }
}

private fun TFLiteDataType.toCoreTensorDataType(): TensorDataType = when (this) {
    TFLiteDataType.FLOAT32 -> TensorDataType.FLOAT32
    TFLiteDataType.INT32 -> TensorDataType.INT32
    TFLiteDataType.UINT8 -> TensorDataType.UINT8
    TFLiteDataType.INT64 -> TensorDataType.INT64
    else -> throw IllegalArgumentException("Unsupported TFLite data type: $this")
}
