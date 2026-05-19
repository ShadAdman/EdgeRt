package org.kmp.playground.kflite

class KfliteClass(
    model: ByteArray,
    options: InterpreterOptions = InterpreterOptions()
) {
    private var interpreter = Interpreter(model, options)
    fun getInputTensorCount(): Int = interpreter.getOutputTensorCount()
    fun getOutputTensorCount(): Int = interpreter.getOutputTensorCount()
    fun getInputTensor(index: Int): Tensor = interpreter.getInputTensor(index)
    fun getOutputTensor(index: Int): Tensor = interpreter.getOutputTensor(index)
    fun resizeInput(index: Int, shape: TensorShape) = interpreter.resizeInput(index, shape)
    fun run(inputs: List<Any>, outputs: Map<Int, Any>) = interpreter.run(inputs, outputs)
    fun close() {
        interpreter.close()
    }
}