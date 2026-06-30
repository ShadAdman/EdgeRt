package org.kmp.playground.kflite.coldstart

import org.kmp.playground.kflite.kflite.KfliteClass
import org.kmp.playground.kflite.tensor.TensorDataType

/**
 * Interface for providing dummy input and output data for dry runs.
 */
interface InputProvider {
    /**
     * Creates dummy input data for a tensor with the given [dataType] and [shape].
     */
    fun createInput(dataType: TensorDataType, shape: IntArray): Any

    /**
     * Creates a dummy container for output data for a tensor with the given [dataType] and [shape].
     */
    fun createOutput(dataType: TensorDataType, shape: IntArray): Any
}

/**
 * Default [InputProvider] that creates zero-filled arrays.
 */
object ZeroInputProvider : InputProvider {
    override fun createInput(dataType: TensorDataType, shape: IntArray): Any = createDummyData(dataType, shape)
    override fun createOutput(dataType: TensorDataType, shape: IntArray): Any = createDummyData(dataType, shape)

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

/**
 * Configuration for the [ColdStartEngine].
 *
 * @property iterations Number of dry runs to perform.
 * @property inputProvider Provider for dummy input and output data.
 * @property closeInterpreter Whether to close the interpreter after dry runs.
 */
data class ColdStartConfig(
    val iterations: Int = 1,
    val inputProvider: InputProvider = ZeroInputProvider,
    val closeInterpreter: Boolean = true
)

/**
 * Engine for performing configurable dry runs (warm-ups) on a model.
 *
 * Example usage:
 * ```kotlin
 * val kflite = KfliteClass()
 * kflite.init(modelSource, InterpreterOptions())
 *
 * ColdStartEngine(kflite).warmUp()
 * ```
 */
class ColdStartEngine(
    private val kflite: KfliteClass,
    private val config: ColdStartConfig = ColdStartConfig()
) {
    /**
     * Performs the warm-up runs as configured and returns the engine instance.
     * Assumes the supplied [KfliteClass] is already initialized.
     */
    fun warmUp(): ColdStartEngine {
        if (!kflite.isInitialized) {
            error("KfliteClass must be initialized before calling warmUp().")
        }

        val inputs = (0 until kflite.getInputTensorCount()).map {
            val tensor = kflite.getInputTensor(it)
            config.inputProvider.createInput(tensor.dataType, tensor.shape)
        }

        val outputs = (0 until kflite.getOutputTensorCount()).associateWith {
            val tensor = kflite.getOutputTensor(it)
            config.inputProvider.createOutput(tensor.dataType, tensor.shape)
        }

        repeat(config.iterations) {
            kflite.run(inputs, outputs)
        }

        if (config.closeInterpreter) {
            kflite.close()
        }

        return this
    }
}
