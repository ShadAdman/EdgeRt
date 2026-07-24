package org.kmp.playground.edgert.sample.face_detection.executorch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import edgertlib.sample.generated.resources.Res
import edgertlib.sample.generated.resources.largest_selfie
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.preprocessing.image.*

/**
 * UltraNet Face Detection Example using PyTorch (ExecuTorch) runtime.
 * UltraNet usually expects 320x240 RGB input.
 */
@Composable
fun UltraNetPyTorchSample() {
    val scope = rememberCoroutineScope()
    val inputImage = imageResource(Res.drawable.largest_selfie)
    val inputWidth = 320
    val inputHeight = 240
    
    scope.launch {
        // Initialize EdgeRt with PyTorch runtime
        EdgeRt.init(
            modelSource = EdgeRtModel.fromResource("composeResources/edgertlib.sample.generated.resources/files/ultranet.pt"),
            options = InterpreterOptions(
                runtime = RuntimeType.EXECUTORCH,
                numThreads = 4,
                delegateType = DelegateType.CPU
            )
        )

        // UltraNet (UltraFace variant) output shapes for 320x240:
        // Output 0 (Scores): [1, 4420, 2]
        // Output 1 (Boxes): [1, 4420, 4]
        val numAnchors = 4420
        val scores = FloatArray(numAnchors * 2)
        val boxes = FloatArray(numAnchors * 4)

        // Prepare input buffer (NHWC float32)
        // Note: Standard UltraNet might expect NCHW. If the model fails or produces poor results, 
        // a transpose to NCHW might be required in preprocessing.
        val modelInputSize = 3 * inputWidth * inputHeight * 4 // 3 channels * float32

        EdgeRt.run(
            inputs = listOf(
                inputImage.imageToScaledByteBuffer(
                    inputWidth = inputWidth,
                    inputHeight = inputHeight,
                    inputAllocateSize = modelInputSize,
                    normalize = true
                )
            ),
            outputs = mapOf(
                0 to scores,
                1 to boxes
            )
        )
        
        println("UltraNet (PyTorch) execution completed.")
        println("Sample scores: ${scores.take(5).joinToString()}")
        println("Sample boxes: ${boxes.take(4).joinToString()}")

        EdgeRt.close()
    }
}
