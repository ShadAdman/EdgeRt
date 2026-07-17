package org.kmp.playground.kflite.interpreter

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*
import org.kmp.playground.kflite.runtime.litert.native.LiteRTNativeRuntime

import kotlinx.cinterop.*

internal interface PlatformInterpreterWrapper {
    val inputTensorCount: Int
    val outputTensorCount: Int
    fun getInputTensor(index: Int): Tensor
    fun getOutputTensor(index: Int): Tensor
    fun resizeInput(index: Int, shape: IntArray)
    fun run(inputs: List<Any>, outputs: Map<Int, Any>)
    fun getMetadata(): ModelMetadata
    fun close()
}

//internal expect fun createTFLiteWrapper(modelSource: ModelSource, options: InterpreterOptions): PlatformInterpreterWrapper

internal class LiteRTInterpreterWrapper(
    modelSource: ModelSource,
    options: InterpreterOptions
) : PlatformInterpreterWrapper {
    private val runtime = LiteRTNativeRuntime(modelSource, options)

    override val inputTensorCount: Int get() = runtime.inputTensorCount
    override val outputTensorCount: Int get() = runtime.outputTensorCount
    override fun getInputTensor(index: Int): Tensor = runtime.getInputTensor(index)
    override fun getOutputTensor(index: Int): Tensor = runtime.getOutputTensor(index)
    override fun resizeInput(index: Int, shape: IntArray) = runtime.resizeInput(index, shape)
    override fun run(inputs: List<Any>, outputs: Map<Int, Any>) = runtime.run(inputs, outputs)
    override fun getMetadata(): ModelMetadata = runtime.getMetadata()
    override fun close() = runtime.close()
}

@OptIn(ExperimentalForeignApi::class)
actual class Interpreter actual constructor(modelSource: ModelSource, options: InterpreterOptions) {

    private val wrapper: PlatformInterpreterWrapper = when (options.runtime) {
        RuntimeType.TFLITE -> LiteRTInterpreterWrapper(modelSource, options)
        RuntimeType.LITERT -> LiteRTInterpreterWrapper(modelSource, options)
    }

    actual constructor(model: ByteArray, options: InterpreterOptions) : this(ByteArraySource(model), options)

    actual fun getInputTensorCount(): Int = wrapper.inputTensorCount
    actual fun getOutputTensorCount(): Int = wrapper.outputTensorCount
    actual fun getInputTensor(index: Int): Tensor = wrapper.getInputTensor(index)
    actual fun getOutputTensor(index: Int): Tensor = wrapper.getOutputTensor(index)
    actual fun resizeInput(index: Int, shape: TensorShape) = wrapper.resizeInput(index, shape.dimensions)
    actual fun run(inputs: List<Any>, outputs: Map<Int, Any>) = wrapper.run(inputs, outputs)
    actual fun getMetadata(): ModelMetadata = wrapper.getMetadata()
    actual fun close() = wrapper.close()
}
