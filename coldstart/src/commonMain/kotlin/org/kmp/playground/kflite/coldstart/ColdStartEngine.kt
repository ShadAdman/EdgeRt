package org.kmp.playground.kflite.coldstart

import org.kmp.playground.kflite.kflite.Kflite
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
 */
class ColdStartEngine(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions = InterpreterOptions(),
    private val config: ColdStartConfig = ColdStartConfig()
) {
    private var kfliteClass: KfliteClass? = null

    /**
     * Performs the warm-up runs on a new [KfliteClass] instance.
     * Returns the engine instance for chaining.
     */
    fun warmUp(): ColdStartEngine {
        val current = kfliteClass ?: KfliteClass().also { kfliteClass = it }
        if (!current.isInitialized) {
            current.init(modelSource, options)
        }

        val inputs = (0 until current.getInputTensorCount()).map {
            val tensor = current.getInputTensor(it)
            config.inputProvider.createInput(tensor.dataType, tensor.shape)
        }

        val outputs = (0 until current.getOutputTensorCount()).associateWith {
            val tensor = current.getOutputTensor(it)
            config.inputProvider.createOutput(tensor.dataType, tensor.shape)
        }

        repeat(config.iterations) {
            current.run(inputs, outputs)
        }

        if (config.closeInterpreter) {
            current.close()
            kfliteClass = null
        }
        return this
    }

    /**
     * Performs the warm-up runs on the global [Kflite] singleton.
     */
    fun warmUpSingleton() {
        Kflite.init(modelSource, options)

        val inputs = (0 until Kflite.getInputTensorCount()).map {
            val tensor = Kflite.getInputTensor(it)
            config.inputProvider.createInput(tensor.dataType, tensor.shape)
        }

        val outputs = (0 until Kflite.getOutputTensorCount()).associateWith {
            val tensor = Kflite.getOutputTensor(it)
            config.inputProvider.createOutput(tensor.dataType, tensor.shape)
        }

        repeat(config.iterations) {
            Kflite.run(inputs, outputs)
        }

        if (config.closeInterpreter) {
            Kflite.close()
        }
    }

    /**
     * Executes the dry runs using the [warmUp] logic.
     */
    fun run() {
        warmUp()
    }

    /**
     * Returns the [KfliteClass] instance if [ColdStartConfig.closeInterpreter] was false during [warmUp].
     */
    fun getKflite(): KfliteClass? = kfliteClass
}
