package org.kmp.playground.kflite

class KfliteClass {
    private var interpreter: Interpreter? = null

    val isInitialized: Boolean
        get() = interpreter != null

    fun init(model: ByteArray,options: InterpreterOptions = InterpreterOptions()) {
        interpreter?.close()
        interpreter = Interpreter(model,options)
    }
    fun getInputTensorCount(): Int = interpreterOrThrow().getOutputTensorCount()
    fun getOutputTensorCount(): Int = interpreterOrThrow().getOutputTensorCount()
    fun getInputTensor(index: Int): Tensor = interpreterOrThrow().getInputTensor(index)
    fun getOutputTensor(index: Int): Tensor = interpreterOrThrow().getOutputTensor(index)
    fun resizeInput(index: Int, shape: TensorShape) = interpreterOrThrow().resizeInput(index, shape)
    fun run(inputs: List<Any>, outputs: Map<Int, Any>) = interpreterOrThrow().run(inputs, outputs)
    fun close() {
        interpreterOrThrow().close()
    }
    private fun interpreterOrThrow(): Interpreter =
        interpreter ?: error("Interpreter not initialized. Call KfLite.init() first.")
}