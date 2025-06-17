package org.kmp.playground.kflite

object Kflite {
    private var interpreter: Interpreter? = null

    fun init(model: ByteArray, options: InterpreterOptions = InterpreterOptions()) {
        check(interpreter == null) { "Interpreter already initialized." }
        interpreter = Interpreter(model, options)
    }

    fun getInputTensorCount(): Int =
        interpreterOrThrow().getInputTensorCount()

    fun getOutputTensorCount(): Int =
        interpreterOrThrow().getOutputTensorCount()

    fun getInputTensor(index: Int): Tensor =
        interpreterOrThrow().getInputTensor(index)

    fun getOutputTensor(index: Int): Tensor =
        interpreterOrThrow().getOutputTensor(index)

    fun resizeInput(index: Int, shape: TensorShape) =
        interpreterOrThrow().resizeInput(index, shape)

    fun run(inputs: List<Any>, outputs: Map<Int, Any>) =
        interpreterOrThrow().run(inputs, outputs)

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun interpreterOrThrow(): Interpreter =
        interpreter ?: error("Interpreter not initialized. Call KfLite.init() first.")
}