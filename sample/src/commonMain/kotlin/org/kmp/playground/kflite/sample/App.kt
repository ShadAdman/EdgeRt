package org.kmp.playground.kflite.sample

import androidx.compose.runtime.Composable
import org.kmp.playground.kflite.sample.face_detection.BlazeFaceLiteRTSample
import org.kmp.playground.kflite.sample.face_detection.BlazeFaceTFLiteSample
import org.kmp.playground.kflite.sample.object_detection.litert.LitertRunModelWithImageSample
import org.kmp.playground.kflite.sample.object_detection.litert.ResourceRunModelSample

@Composable
fun App() {
    // Note: For memory-mapped resource loading on Android, we need the full asset path.
    // ResourceRunModelSample avoids copying bytes by mapping from bundle/assets.
    // ResourceRunModelSample("composeResources/kflitelib.sample.generated.resources/files/efficientdet-lite2.tflite")

    // BlazeFace Face Detection Examples
//    BlazeFaceTFLiteSample()
    LitertRunModelWithImageSample()
    // BlazeFaceLiteRTSample()
}
