package org.kmp.playground.kflite.sample.object_detection.tflite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kflitelib.sample.generated.resources.Res
import kflitelib.sample.generated.resources.example_model_input
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.preprocessing.image.*

@Composable
fun TFLiteRunModelWithImageSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.example_model_input)
    scope.launch {
        Kflite.init(
            modelSource = KFliteModel.fromResource("composeResources/kflitelib.sample.generated.resources/files/efficientdet-lite2.tflite"),
            options = InterpreterOptions(
                runtime = RuntimeType.TFLITE,
                numThreads = 4,
                delegateType = DelegateType.NNAPI_COREML,
                allowFp16PrecisionForFp32 = true
            )
        )

        println("TensorInputCount: ${Kflite.getInputTensorCount()}")
        println("TensorOutputCount: ${Kflite.getOutputTensorCount()}")

        val inputImageWidth = Kflite.getInputTensor(0).shape[1]
        val inputImageHeight = Kflite.getInputTensor(0).shape[2]
        val modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

        val firstOutputShape = Kflite.getOutputTensor(0).shape[0]
        val secondOutputShape = Kflite.getOutputTensor(0).shape[1]
        val thirdOutputShape = Kflite.getOutputTensor(0).shape[2]

        val modelOutputSize = Array(firstOutputShape) {
            Array(secondOutputShape) {
                FloatArray(thirdOutputShape)
            }
        }

        // new api for image preprocessing
//        val inputBuffer = inputImage.preprocess(allocateSize = 640 * 640 * 3 * 4) {
//            rotate(90f)
//            crop(x = 0, y = 0, width = 300, height = 300)
//            resize(width = 640, height = 640)
//            normalize = true
//        }

        Kflite.run(
            listOf(
                inputImage.imageToScaledByteBuffer(
                    inputWidth = inputImageWidth,
                    inputHeight = inputImageHeight,
                    inputAllocateSize = modelInputSize
                )
            ),
            mapOf(Pair(0, modelOutputSize))
        )
        println("Output of first detection: ${modelOutputSize[0][0].joinToString()}")

        Kflite.close()
    }
}

@Composable
fun MemoryMappedRunModelWithImageSample(modelPath: String) {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.example_model_input)
    val inputImageSize = 448
    scope.launch {
        // Load model directly from file path without copying to memory
        Kflite.init(
            modelSource = KFliteModel.fromFile(modelPath),
            options = InterpreterOptions(
                runtime = RuntimeType.TFLITE,
                numThreads = 4,
                delegateType = DelegateType.NNAPI_COREML
            )
        )

        val modelInputSize = FLOAT_TYPE_SIZE * inputImageSize * inputImageSize * PIXEL_SIZE
        val modelOutputSize = Array(1) { Array(25) { FloatArray(4) } }

        // new api for image preprocessing
//        val inputBuffer = inputImage.preprocess(allocateSize = 640 * 640 * 3 * 4) {
//            rotate(90f)
//            crop(x = 0, y = 0, width = 300, height = 300)
//            resize(width = 640, height = 640)
//            normalize = true
//        }

        Kflite.run(
            listOf(inputImage.imageToScaledByteBuffer(inputImageSize, inputImageSize, modelInputSize)),
            mapOf(Pair(0, modelOutputSize))
        )

        Kflite.close()
    }
}

private const val FLOAT_TYPE_SIZE = 3
private const val PIXEL_SIZE = 1
