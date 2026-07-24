package org.kmp.playground.kflite.sample

import androidx.compose.runtime.Composable
import org.kmp.playground.kflite.sample.face_detection.executorch.YoloV5ExecuTorchSample
import org.kmp.playground.kflite.sample.face_detection.pytorch.YoloV5PyTorchSample

@Composable
fun App() {
    YoloV5PyTorchSample()
}
