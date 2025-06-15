package org.kmp.playground.kflite

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import cocoapods.TFLTensorFlowLite.TFLInterpreter
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.*


@OptIn(ExperimentalForeignApi::class)
actual class Interpreter actual constructor(model: ByteArray, options: InterpreterOptions) {

    @OptIn(ExperimentalForeignApi::class)
    private val tflInterpreter: PlatformInterpreter = errorHandled { errPtr ->
        PlatformInterpreter(model.writeToTempFile(), options.tflInterpreterOptions, errPtr)
    }!!

    init {

        errorHandled { errPtr ->
            tflInterpreter.allocateTensorsWithError(errPtr)
        }
    }

    /**
     * Gets the number of input tensors.
     */
    actual fun getInputTensorCount(): Int {
        return tflInterpreter.inputTensorCount().toInt()
    }

    /**
     * Gets the number of output Tensors.
     */
    actual fun getOutputTensorCount(): Int {
        return tflInterpreter.outputTensorCount().toInt()
    }

    /**
     * Gets the Tensor associated with the provdied input index.
     *
     * @throws IllegalArgumentException if [index] is negative or is not smaller than the
     * number of model inputs.
     */
    actual fun getInputTensor(index: Int): Tensor {
        return errorHandled { errPtr ->
            tflInterpreter.inputTensorAtIndex(index.toULong(), errPtr)
        }!!.toTensor()
    }

    /**
     * Gets the Tensor associated with the provdied output index.
     *
     * @throws IllegalArgumentException if [index] is negative or is not smaller than the
     * number of model inputs.
     */
    actual fun getOutputTensor(index: Int): Tensor {
        return errorHandled { errPtr ->
            tflInterpreter.outputTensorAtIndex(index.toULong(), errPtr)
        }!!.toTensor()
    }

    /**
     * Resizes [index] input of the native model to the given [shape].
     */
    actual fun resizeInput(index: Int, shape: TensorShape) {
        errorHandled { errPtr ->
            tflInterpreter.resizeInputTensorAtIndex(
                index.toULong(),
                shape.getNSNumberDimensionList(),
                errPtr
            )
        }
    }

    /**
     * Runs model inference if the model takes multiple inputs, or returns multiple outputs.
     *
     * TODO: need to implement [outputs] applying.
     */
    actual fun run(
        inputs: List<Any>,
        outputs: Map<Int, Any>
    ) {
        if (inputs.size > getInputTensorCount()) throw IllegalArgumentException("Wrong inputs dimension.")

        inputs.forEachIndexed { index, input ->
            val inputTensor = getInputTensor(index)
            println("inputTensor: $input")
            errorHandled { errPtr ->
                inputTensor.platformTensor.copyData(
                    input as NSData,
                    errPtr
                )
            }
        }

        // Run inference
        errorHandled { errPtr ->
            tflInterpreter.invokeWithError(errPtr)
        }

        // Collect outputs
        outputs.forEach { (index, outputContainer) ->
            val outputTensor = getOutputTensor(index)
            val outputData = errorHandled { errPtr ->
                outputTensor.platformTensor.dataWithError(errPtr)
            } ?: error("Failed to get output tensor data")

            val outputByteArray = ByteArray(outputData.length.toInt())
            outputData.bytes?.reinterpret<ByteVar>()?.readBytes(outputData.length.toInt())
                ?.copyInto(outputByteArray)

            val typedOutput: Any = when (outputTensor.dataType) {
                TensorDataType.FLOAT32 -> {
                    val floatCount = outputByteArray.size / Float.SIZE_BYTES
                    val buffer = outputByteArray.usePinned {
                        it.addressOf(0).reinterpret<FloatVar>().readBytes(floatCount)
                    }
                    buffer.toFloatArray()
                }

                TensorDataType.INT32 -> {
                    val intCount = outputByteArray.size / Int.SIZE_BYTES
                    val buffer = outputByteArray.usePinned {
                        it.addressOf(0).reinterpret<IntVar>().readBytes(intCount)
                    }
                    buffer.toIntArray()
                }

                TensorDataType.UINT8 -> {
                    outputByteArray.map { it.toUByte() }.toUByteArray()
                }

                TensorDataType.INT64 -> {
                    val longCount = outputByteArray.size / Long.SIZE_BYTES
                    val buffer = outputByteArray.usePinned {
                        it.addressOf(0).reinterpret<LongVar>().readBytes(longCount)
                    }
                    buffer.toLongArray()
                }
            }

            // Assign the result to output container
            when (outputContainer) {
                is FloatArray -> (typedOutput as FloatArray).copyInto(outputContainer)
                is IntArray -> (typedOutput as IntArray).copyInto(outputContainer)
                is LongArray -> (typedOutput as LongArray).copyInto(outputContainer)
                is UIntArray -> (typedOutput as UIntArray).copyInto(outputContainer)
                is Array<*> -> {
                    if (outputContainer.isNotEmpty() && outputContainer[0] is Array<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val outer = outputContainer as Array<Array<FloatArray>>
                        val flat = typedOutput as FloatArray

                        val totalSize = outer.size * outer[0].size * outer[0][0].size
                        require(flat.size == totalSize) {
                            println("The outer size is ${outer.size} * ${outer[0].size} * ${outer[0][0].size}")
                            "Flat array size (${flat.size}) does not match output container size ($totalSize)"
                        }

                        var index = 0
                        for (i in outer.indices) {
                            for (j in outer[i].indices) {
                                for (k in outer[i][j].indices) {
                                    outer[i][j][k] = flat[index++]
                                }
                            }
                        }
                    } else {
                        error("Unsupported array shape in output container: ${outputContainer::class}")
                    }
                }
                else -> error("Unsupported output container type: ${outputContainer::class}")
            }
        }
    }

    /**
     * Release resources associated with the [Interpreter].
     */
    actual fun close() {
        // TODO: ???
    }
}