package org.kmp.playground.kflite

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import cocoapods.TFLTensorFlowLite.TFLInterpreter

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

        inputs.forEachIndexed { index, any ->
            val inputTensor = getInputTensor(index)
            errorHandled { errPtr ->
                inputTensor.platformTensor.copyData(
                    any as NSData,
                    errPtr
                ) // Fixme: hardcast Any to NSData
            }
        }

        errorHandled { errPtr ->
            tflInterpreter.invokeWithError(errPtr)
        }

        inputs.forEachIndexed { index, any ->
            val outputTensor = getOutputTensor(index)

            val array = when (outputTensor.dataType) {
                TensorDataType.FLOAT32 -> {
                    errorHandled { errPtr ->
                        outputTensor.platformTensor.dataWithError(errPtr)
                    }!!.toUByteArray().toFloatArray()
                }

                TensorDataType.INT32 -> IntArray(outputTensor.dataType.byteSize()) // Fixme:
                TensorDataType.UINT8 -> UIntArray(outputTensor.dataType.byteSize()) // Fixme:
                TensorDataType.INT64 -> LongArray(outputTensor.dataType.byteSize()) // Fixme:
            }

            (outputs[0] as Array<Any>)[0] =
                array // TODO: hardcoded case, works only with digits sample
        }
    }

    /**
     * Release resources associated with the [Interpreter].
     */
    actual fun close() {
        // TODO: ???
    }
}