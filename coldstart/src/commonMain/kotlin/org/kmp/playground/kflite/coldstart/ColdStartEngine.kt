package org.kmp.playground.kflite.coldstart

import org.kmp.playground.kflite.kflite.KfliteClass
import org.kmp.playground.kflite.model.ModelSource
import org.kmp.playground.kflite.interpreter.InterpreterOptions
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
 * val engine = ColdStartEngine(
 *     modelSource = model,
 *     options = InterpreterOptions(),
 *     config = ColdStartConfig(
 *         iterations = 5,
 *         inputProvider = ZeroInputProvider,
 *         closeInterpreter = true
 *     )
 * ).warmUp()
 * ```
 */
class ColdStartEngine(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions = InterpreterOptions(),
    private val config: ColdStartConfig = ColdStartConfig()
) {
    private var kflite: KfliteClass? = null

    /**
     * Performs the warm-up runs as configured and returns the engine instance.
     */
    fun warmUp(): ColdStartEngine {
        run()
        return this
    }

    /**
     * Executes the dry runs.
     */
    fun run() {
        val currentKflite = kflite ?: KfliteClass().also { kflite = it }
        if (!currentKflite.isInitialized) {
            currentKflite.init(modelSource, options)
        }

        val inputs = mutableListOf<Any>()
        for (i in 0 until currentKflite.getInputTensorCount()) {
            val tensor = currentKflite.getInputTensor(i)
            inputs.add(config.inputProvider.createInput(tensor.dataType, tensor.shape))
        }

        val outputs = mutableMapOf<Int, Any>()
        for (i in 0 until currentKflite.getOutputTensorCount()) {
            val tensor = currentKflite.getOutputTensor(i)
            outputs[i] = config.inputProvider.createOutput(tensor.dataType, tensor.shape)
        }

        repeat(config.iterations) {
            currentKflite.run(inputs, outputs)
        }

        if (config.closeInterpreter) {
            currentKflite.close()
            kflite = null
        }
    }

    /**
     * Returns the [KfliteClass] instance if [ColdStartConfig.closeInterpreter] was false.
     */
    fun getKflite(): KfliteClass? = kflite
}
