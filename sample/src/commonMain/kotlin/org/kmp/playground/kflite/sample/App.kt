package org.kmp.playground.kflite.sample

import androidx.compose.runtime.Composable

@Composable
fun App() {
    // Note: For memory-mapped resource loading on Android, we need the full asset path.
    // ResourceRunModelSample avoids copying bytes by mapping from bundle/assets.
    ResourceRunModelSample("composeResources/kflitelib.sample.generated.resources/files/efficientdet-lite2.tflite")
}
