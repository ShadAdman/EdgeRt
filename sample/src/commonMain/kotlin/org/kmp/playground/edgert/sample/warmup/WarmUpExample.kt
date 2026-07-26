package org.kmp.playground.kflite.sample.warmup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kflitelib.sample.generated.resources.Res
import kflitelib.sample.generated.resources.example_model_input
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.preprocessing.image.*

@Composable
fun WarmUpModelWithImageSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.example_model_input)
    val inputImageSize = 448
    
    scope.launch {
        Kflite.init(
            modelSource = KFliteModel.fromResource("composeResources/kflitelib.sample.generated.resources/files/efficientdet-lite2.tflite"),
            options = InterpreterOptions(runtime = RuntimeType.LITERT)
        )

        val modelInputSize = 3 * inputImageSize * inputImageSize * 1
        val inputBuffer = inputImage.imageToScaledByteBuffer(inputImageSize, inputImageSize, modelInputSize)

        // Warm up with 5 iterations using the actual image data
        Kflite.warmUp(
            config = WarmUpConfig(
                iterations = 5,
                inputProvider = ImageInputProvider(inputBuffer),
                closeInterpreter = false // Keep it open for real inference later
            )
        )
        
        println("Model warmed up and ready!")
    }
}

@Composable
fun WakeupModelWithImageSample() {
    val scope = rememberCoroutineScope()

    scope.launch {
        Kflite.init(
            modelSource = KFliteModel.fromResource("composeResources/kflitelib.sample.generated.resources/files/efficientdet-lite2.tflite"),
            options = InterpreterOptions(runtime = RuntimeType.TFLITE)
        )
        // Wake up triggers delegate initialization and pre-touches tensors
        Kflite.wakeUp()

        println("Model woken up!")
    }
}
