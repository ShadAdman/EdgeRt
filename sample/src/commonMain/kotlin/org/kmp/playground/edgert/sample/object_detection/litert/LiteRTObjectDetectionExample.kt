package org.kmp.playground.edgert.sample.object_detection.litert

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import edgertlib.sample.generated.resources.Res
import edgertlib.sample.generated.resources.example_model_input
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.kmp.playground.edgert.kflite.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.preprocessing.image.*

@Composable
fun LitertRunModelWithImageSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.example_model_input)
    val inputImageSize = 448
    scope.launch {
        EdgeRt.init(
            modelSource = EdgeRtModel.fromResource("composeResources/edgertlib.sample.generated.resources/files/efficientdet-lite2.tflite"),
            options = InterpreterOptions(
                runtime = RuntimeType.LITERT,
                numThreads = 4,
                delegateType = DelegateType.NNAPI_COREML,
                allowFp16PrecisionForFp32 = true
            )
        )

        println("TensorInputCount: ${EdgeRt.getInputTensorCount()}")
        println("TensorOutputCount: ${EdgeRt.getOutputTensorCount()}")

        val modelInputSize = FLOAT_TYPE_SIZE * inputImageSize * inputImageSize * PIXEL_SIZE
        val modelOutputSize = Array(1) { Array(25) { FloatArray(4) } }

          // new api for image preprocessing
//        val inputBuffer = inputImage.preprocess(allocateSize = 640 * 640 * 3 * 4) {
//            rotate(90f)
//            crop(x = 0, y = 0, width = 300, height = 300)
//            resize(width = 640, height = 640)
//            normalize = true
//        }

        EdgeRt.run(
            listOf(
                inputImage.imageToScaledByteBuffer(
                    inputWidth = inputImageSize,
                    inputHeight = inputImageSize,
                    inputAllocateSize = modelInputSize
                )
            ),
            mapOf(Pair(0, modelOutputSize))
        )
        println("Output of first detection: ${modelOutputSize[0][0].joinToString()}")

        EdgeRt.close()
    }
}

@Composable
fun ResourceRunModelSample(resourcePath: String) {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.example_model_input)
    val inputImageSize = 448
    scope.launch {
        // Load model from bundle/assets without copying to memory
        EdgeRt.init(
            modelSource = EdgeRtModel.fromResource(resourcePath),
            options = InterpreterOptions(
                runtime = RuntimeType.LITERT,
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

        EdgeRt.run(
            listOf(inputImage.imageToScaledByteBuffer(inputImageSize, inputImageSize, modelInputSize)),
            mapOf(Pair(0, modelOutputSize))
        )

        println("Output of first detection: ${modelOutputSize[0][0].joinToString()}")

        EdgeRt.close()
    }
}

private const val FLOAT_TYPE_SIZE = 3
private const val PIXEL_SIZE = 1
