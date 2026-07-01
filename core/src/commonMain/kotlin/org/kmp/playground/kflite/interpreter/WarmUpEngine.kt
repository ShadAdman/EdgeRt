package org.kmp.playground.kflite.interpreter

/**
 * Configuration for the [WarmUpEngine].
 *
 * @property iterations Number of dry runs to perform.
 * @property inputProvider Provider for dummy input and output data.
 * @property closeInterpreter Whether to close the interpreter after dry runs.
 * @property runDummyInference Whether to run a single dummy inference pass during [WarmUpEngine.wakeUp].
 */
data class WarmUpConfig(
    val iterations: Int = 1,
    val inputProvider: InputProvider = ZeroInputProvider,
    val closeInterpreter: Boolean = true,
    val runDummyInference: Boolean = false
)

/**
 * Engine for performing configurable dry runs (warm-ups) and lightweight preparation (wake-ups) on a model.
 */
class WarmUpEngine(
    private val interpreter: Interpreter,
    private val config: WarmUpConfig = WarmUpConfig()
) {
    private var isWakedUp = false

    /**
     * Performs lightweight runtime preparation before inference.
     * This triggers delegate initialization, pre-touches tensors, and optionally runs a single dummy inference.
     * This operation is idempotent and safe to call multiple times.
     */
    fun wakeUp(): WarmUpEngine {
        if (isWakedUp) return this
        
        // 1. Pre-touch tensors (triggers internal allocation and metadata loading)
        val inputCount = interpreter.getInputTensorCount()
        val outputCount = interpreter.getOutputTensorCount()
        
        for (i in 0 until inputCount) {
            val tensor = interpreter.getInputTensor(i)
            tensor.name
            tensor.shape
            tensor.dataType
        }
        
        for (i in 0 until outputCount) {
            val tensor = interpreter.getOutputTensor(i)
            tensor.name
            tensor.shape
            tensor.dataType
        }

        // 2. Trigger runtime/delegate setup
        // Note: getMetadata() call removed if not present in common Interpreter
        // If it's needed, it should be added to Interpreter.kt

        // 3. Optional: single dummy execution
        if (config.runDummyInference) {
            runSingleInference(interpreter)
        }

        isWakedUp = true
        return this
    }

    /**
     * Performs the warm-up runs as configured and returns the engine instance.
     * Assumes the supplied interpreter is already initialized.
     */
    fun warmUp(): WarmUpEngine {
        repeat(config.iterations) {
            runSingleInference(interpreter)
        }

        if (config.closeInterpreter) {
            interpreter.close()
        }

        return this
    }

    private fun runSingleInference(interpreter: Interpreter) {
        val inputs = (0 until interpreter.getInputTensorCount()).map {
            val tensor = interpreter.getInputTensor(it)
            config.inputProvider.createInput(tensor, it)
        }

        val outputs = (0 until interpreter.getOutputTensorCount()).associateWith {
            val tensor = interpreter.getOutputTensor(it)
            config.inputProvider.createOutput(tensor, it)
        }

        interpreter.run(inputs, outputs)
    }
}
