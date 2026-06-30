package org.kmp.playground.kflite.coldstart

import org.kmp.playground.kflite.kflite.KfliteClass
import org.kmp.playground.kflite.model.ModelSource
import org.kmp.playground.kflite.interpreter.InterpreterOptions
import org.kmp.playground.kflite.tensor.Tensor
import org.kmp.playground.kflite.tensor.TensorDataType

/**
 * Information about a tensor used for creating warm-up data.
 */
data class TensorInfo(
    val name: String,
    val shape: IntArray,
    val dataType: TensorDataType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TensorInfo
        if (name != other.name) return false
        if (!shape.contentEquals(other.shape)) return false
        if (dataType != other.dataType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + shape.contentHashCode()
        result = 31 * result + dataType.hashCode()
        return result
    }
}

/**
 * Interface for providing dummy input and output data for dry runs.
 */
interface InputProvider {
    /**
     * Creates dummy input data for the given tensor.
     */
    fun createInput(tensor: TensorInfo): Any

    /**
     * Creates a dummy container for output data for the given tensor.
     */
    fun createOutput(tensor: TensorInfo): Any
}

/**
 * Default [InputProvider] that creates zero-filled arrays.
 */
object ZeroInputProvider : InputProvider {
    override fun createInput(tensor: TensorInfo): Any = createDummyData(tensor.dataType, tensor.shape)
    override fun createOutput(tensor: TensorInfo): Any = createDummyData(tensor.dataType, tensor.shape)

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
class ColdStartEngine internal constructor(
    private val modelSource: ModelSource,
    private val options: InterpreterOptions,
    private val config: ColdStartConfig,
    private val kfliteProxyFactory: () -> KfliteProxy
) {
    constructor(
        modelSource: ModelSource,
        options: InterpreterOptions = InterpreterOptions(),
        config: ColdStartConfig = ColdStartConfig()
    ) : this(modelSource, options, config, { KfliteClassProxy(KfliteClass()) })

    private var proxy: KfliteProxy? = null

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
        val currentProxy = proxy ?: kfliteProxyFactory().also { proxy = it }
        if (!currentProxy.isInitialized) {
            currentProxy.init(modelSource, options)
        }

        val inputs = mutableListOf<Any>()
        for (i in 0 until currentProxy.getInputTensorCount()) {
            val tensorInfo = currentProxy.getInputTensorInfo(i)
            inputs.add(config.inputProvider.createInput(tensorInfo))
        }

        val outputs = mutableMapOf<Int, Any>()
        for (i in 0 until currentProxy.getOutputTensorCount()) {
            val tensorInfo = currentProxy.getOutputTensorInfo(i)
            outputs[i] = config.inputProvider.createOutput(tensorInfo)
        }

        repeat(config.iterations) {
            currentProxy.run(inputs, outputs)
        }

        if (config.closeInterpreter) {
            currentProxy.close()
            proxy = null
        }
    }

    /**
     * Returns the [KfliteClass] instance if [ColdStartConfig.closeInterpreter] was false.
     * Note: This only works when using the default constructor.
     */
    fun getKflite(): KfliteClass? {
        return (proxy as? KfliteClassProxy)?.kflite
    }
}

internal interface KfliteProxy {
    val isInitialized: Boolean
    fun init(modelSource: ModelSource, options: InterpreterOptions)
    fun getInputTensorCount(): Int
    fun getOutputTensorCount(): Int
    fun getInputTensorInfo(index: Int): TensorInfo
    fun getOutputTensorInfo(index: Int): TensorInfo
    fun run(inputs: List<Any>, outputs: Map<Int, Any>)
    fun close()
}

private class KfliteClassProxy(val kflite: KfliteClass) : KfliteProxy {
    override val isInitialized: Boolean get() = kflite.isInitialized
    override fun init(modelSource: ModelSource, options: InterpreterOptions) = kflite.init(modelSource, options)
    override fun getInputTensorCount(): Int = kflite.getInputTensorCount()
    override fun getOutputTensorCount(): Int = kflite.getOutputTensorCount()
    override fun getInputTensorInfo(index: Int): TensorInfo = kflite.getInputTensor(index).toTensorInfo()
    override fun getOutputTensorInfo(index: Int): TensorInfo = kflite.getOutputTensor(index).toTensorInfo()
    override fun run(inputs: List<Any>, outputs: Map<Int, Any>) = kflite.run(inputs, outputs)
    override fun close() = kflite.close()
}

private fun Tensor.toTensorInfo() = TensorInfo(name, shape, dataType)
