package org.kmp.playground.kflite

import org.tensorflow.lite.RuntimeFlavor
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.kmp.playground.kflite.PlatformInterpreterOptions as AndroidPlatformInterpreterOptions

actual class InterpreterOptions(
    numThreads: Int = 4,
    delegateType: DelegateType = DelegateType.CPU,
    allowFp16PrecisionForFp32: Boolean = false,
    allowBufferHandleOutput: Boolean = true,
) {
    actual constructor(
        numThreads: Int,
        delegateType: DelegateType,
        allowFp16PrecisionForFp32: Boolean
    ) : this(
        numThreads = numThreads,
        delegateType = delegateType,
        allowFp16PrecisionForFp32 = allowFp16PrecisionForFp32,
        allowBufferHandleOutput = true
    )

    val compatList = CompatibilityList()

    internal val tensorFlowInterpreterOptions = AndroidPlatformInterpreterOptions()

        .setAllowBufferHandleOutput(allowBufferHandleOutput)
        .apply {
            when (delegateType) {
                DelegateType.CPU -> {
                    setNumThreads(numThreads)
                }

                DelegateType.GPU_METAL -> {
                    if (compatList.isDelegateSupportedOnThisDevice) {
                        val gpuOptions = GpuDelegateFactory.Options().apply {
                            isPrecisionLossAllowed =
                                allowFp16PrecisionForFp32
                            /*
                            - Can be a feature to set by consumer
                            telling TensorFlow Lite how to balance speed ,battery and latency when running model on the GPU.
                                INFERENCE_PREFERENCE_SUSTAINED_SPEED → Optimize for long-running, repeated inferences (like live camera input)
                                INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER → Optimize for a single, fast inference.
                                INFERENCE_PREFERENCE_LOW_POWER → Prioritize energy efficiency.
                             */
                            inferencePreference =
                                GpuDelegateFactory.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED
                        }
                        addDelegate(GpuDelegateFactory(gpuOptions).create(RuntimeFlavor.APPLICATION))
                    }
                }

                DelegateType.NNAPI_COREML -> {
                    if (compatList.isDelegateSupportedOnThisDevice) {
                        val nnOptions = NnApiDelegate.Options().apply {
                            allowFp16 = allowFp16PrecisionForFp32
                        }
                        addDelegate(NnApiDelegate(nnOptions))
                    }
                }
            }
        }
}
