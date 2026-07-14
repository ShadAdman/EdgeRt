package org.kmp.playground.kflite.sample.face_detection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kflitelib.sample.generated.resources.Res
import kflitelib.sample.generated.resources.largest_selfie
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.preprocessing.image.*

@Composable
fun BlazeFaceTFLiteSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.largest_selfie)

    scope.launch {
        Kflite.init(
            modelSource = KFliteModel.fromResource("composeResources/kflitelib.sample.generated.resources/files/blaze_face_full_range.tflite"),
            options = InterpreterOptions(
                runtime = RuntimeType.TFLITE,
                numThreads = 4,
                delegateType = DelegateType.CPU
            )
        )

        val inputTensor = Kflite.getInputTensor(0)
        val inputWidth = inputTensor.shape[1]
        val inputHeight = inputTensor.shape[2]

        println("BlazeFace TFLite Input shape: ${inputTensor.shape.joinToString()}")

        val modelInputSize = 4 * inputWidth * inputHeight * 3 // FLOAT_SIZE * W * H * Channels

        // Prepare outputs dynamically based on model
        val outputs = mutableMapOf<Int, Any>()
        for (i in 0 until Kflite.getOutputTensorCount()) {
            val shape = Kflite.getOutputTensor(i).shape
            println("BlazeFace TFLite Output $i shape: ${shape.joinToString()}")
            // For BlazeFace full range:
            // Output 0: [1, 2304, 1] (scores)
            // Output 1: [1, 2304, 16] (regressors/boxes/landmarks)
            if (shape.size == 3) {
                outputs[i] = Array(shape[0]) { Array(shape[1]) { FloatArray(shape[2]) } }
            }
        }

        Kflite.run(
            listOf(
                inputImage.imageToScaledByteBuffer(
                    inputWidth = inputWidth,
                    inputHeight = inputHeight,
                    inputAllocateSize = modelInputSize,
                    normalize = true
                )
            ),
            outputs
        )

        println("BlazeFace TFLite run completed")
        outputs.forEach { (index, data) ->
            if (data is Array<*> && data.isNotEmpty()) {
                val firstBatch = data[0] as Array<*>
                if (firstBatch.isNotEmpty()) {
                    val firstElement = firstBatch[0] as FloatArray
                    println("Output $index first element: ${firstElement.joinToString()}")
                }
            }
        }

        Kflite.close()
    }
}

@Composable
fun BlazeFaceLiteRTSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.largest_selfie)

    scope.launch {
        Kflite.init(
            modelSource = KFliteModel.fromResource("composeResources/kflitelib.sample.generated.resources/files/blaze_face_full_range.tflite"),
            options = InterpreterOptions(
                runtime = RuntimeType.LITERT,
                numThreads = 4,
                delegateType = DelegateType.CPU
            )
        )

        // For BlazeFace full range (192x192)
        val inputWidth = 192
        val inputHeight = 192
        val modelInputSize = 4 * inputWidth * inputHeight * 3

        // Pre-defined output buffers for BlazeFace full range
        val scores = Array(1) { Array(2304) { FloatArray(1) } }
        val regressors = Array(1) { Array(2304) { FloatArray(16) } }

        Kflite.run(
            listOf(
                inputImage.imageToScaledByteBuffer(
                    inputWidth = inputWidth,
                    inputHeight = inputHeight,
                    inputAllocateSize = modelInputSize,
                    normalize = true
                )
            ),
            mapOf(
                0 to scores,
                1 to regressors
            )
        )

        println("BlazeFace LiteRT run completed")
        println("First score: ${scores[0][0][0]}")
        println("First regressor: ${regressors[0][0].joinToString()}")

        Kflite.close()
    }
}
