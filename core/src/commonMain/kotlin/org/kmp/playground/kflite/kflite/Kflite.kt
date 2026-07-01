package org.kmp.playground.kflite.kflite

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

object Kflite {
    var interpreter: Interpreter? = null

    val isInitialized: Boolean
        get() = interpreter != null

    fun init(model: ByteArray, options: InterpreterOptions = InterpreterOptions()) {
        init(KFliteModel.fromBytes(model), options)
    }

    fun init(modelSource: ModelSource, options: InterpreterOptions = InterpreterOptions()) {
        interpreter?.close()
        interpreter = Interpreter(modelSource, options)
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

    fun getMetadata(): ModelMetadata =
        interpreterOrThrow().getMetadata()

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun interpreterOrThrow(): Interpreter =
        interpreter ?: error("Interpreter not initialized. Call KfLite.init() first.")
}



