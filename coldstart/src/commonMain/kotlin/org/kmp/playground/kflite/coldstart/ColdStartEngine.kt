package org.kmp.playground.kflite.coldstart

import org.kmp.playground.kflite.kflite.KfliteClass
import org.kmp.playground.kflite.model.ModelSource
import org.kmp.playground.kflite.interpreter.InterpreterOptions
import org.kmp.playground.kflite.tensor.TensorDataType

class ColdStartEngine(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions = InterpreterOptions()
) {
    fun runDryRuns(iterations: Int = 1) {
        val kflite = KfliteClass()
        kflite.init(modelSource, options)

        val inputs = mutableListOf<Any>()
        for (i in 0 until kflite.getInputTensorCount()) {
            val tensor = kflite.getInputTensor(i)
            inputs.add(createDummyData(tensor.dataType, tensor.shape))
        }

        val outputs = mutableMapOf<Int, Any>()
        for (i in 0 until kflite.getOutputTensorCount()) {
            val tensor = kflite.getOutputTensor(i)
            outputs[i] = createDummyData(tensor.dataType, tensor.shape)
        }

        repeat(iterations) {
            kflite.run(inputs, outputs)
        }

        kflite.close()
    }

    private fun createDummyData(dataType: TensorDataType, shape: IntArray): Any {
        val totalElements = shape.fold(1) { acc, i -> acc * (if (i <= 0) 1 else i) }
        return when (dataType) {
            TensorDataType.FLOAT32 -> FloatArray(totalElements)
            TensorDataType.INT32 -> IntArray(totalElements)
            TensorDataType.UINT8 -> ByteArray(totalElements)
            TensorDataType.INT64 -> LongArray(totalElements)
        }
    }
}
