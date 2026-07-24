package org.kmp.playground.kflite.sample.face_detection.pytorch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kflitelib.sample.generated.resources.Res
import kflitelib.sample.generated.resources.largest_selfie
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.preprocessing.image.*

/**
 * UltraNet Face Detection Example using PyTorch runtime.
 * UltraNet usually expects 320x240 RGB input.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun YoloV5PyTorchSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.largest_selfie)
    val inputWidth = 640
    val inputHeight = 640

    scope.launch {
        // Load model bytes from Compose Resources
        val modelBytes = Res.readBytes("files/yolov5s.pt")

        // Initialize Kflite with ExecuTorch runtime
        Kflite.init(
            modelSource = KFliteModel.fromBytes(modelBytes),
            options = InterpreterOptions(
                runtime = RuntimeType.PYTORCH,
                numThreads = 4,
                delegateType = DelegateType.CPU
            )
        )

        // YoloV5 output shape depends on the specific model export.
        // Assuming [1, 25200, 85] or similar for detection.
        // For face detection it might be different.
        val outputs = mapOf(
            0 to FloatArray(1 * 25200 * 85)
        )

        // Prepare input buffer (NHWC float32)
        val modelInputSize = 3 * inputWidth * inputHeight * 4 // 3 channels * float32

        Kflite.run(
            inputs = listOf(
                inputImage.imageToScaledByteBuffer(
                    inputWidth = inputWidth,
                    inputHeight = inputHeight,
                    inputAllocateSize = modelInputSize,
                    normalize = true
                )
            ),
            outputs = outputs
        )

        println("YoloV5 (ExecuTorch) execution completed.")

        Kflite.close()
    }
}
