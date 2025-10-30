package org.kmp.playground.kflite

import org.tensorflow.lite.RuntimeFlavor
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.kmp.playground.kflite.PlatformInterpreterOptions as AndroidPlatformInterpreterOptions

actual class InterpreterOptions(
    numThreads: Int = 4,
    val delegateType: DelegateType = DelegateType.CPU,
    val inferencePreferenceType: TFLiteInferencePreference = TFLiteInferencePreference.PLATFORM_DEFAULT,
    val allowQuantizedModels: Boolean = true,
    val allowFp16PrecisionForFp32: Boolean = false,
    val allowBufferHandleOutput: Boolean = true,
) {
    actual constructor(
        numThreads: Int,
        delegateType: DelegateType,
        inferencePreferenceType: TFLiteInferencePreference,
        allowQuantizedModels: Boolean,
        allowFp16PrecisionForFp32: Boolean
    ) : this(
        numThreads = numThreads,
        delegateType = delegateType,
        inferencePreferenceType = inferencePreferenceType,
        allowQuantizedModels = allowQuantizedModels,
        allowFp16PrecisionForFp32 = allowFp16PrecisionForFp32,
        allowBufferHandleOutput = true
    )

    val compatList = CompatibilityList()

    internal val tensorFlowInterpreterOptions = AndroidPlatformInterpreterOptions()
        .setNumThreads(numThreads)
        .setAllowBufferHandleOutput(allowBufferHandleOutput)
        .apply {
            when (delegateType) {
                DelegateType.CPU -> Unit
                DelegateType.GPU_METAL -> {
                    setGpuDelegation()?.let {
                        addDelegate(GpuDelegateFactory(it).create(RuntimeFlavor.APPLICATION))
                    }
                }

                DelegateType.NNAPI_COREML -> {
                    setNnApiDelegation()?.let {
                        addDelegate(NnApiDelegate(it))
                    }
                }
            }
        }

    private fun setGpuDelegation() : GpuDelegateFactory.Options? {
        if (compatList.isDelegateSupportedOnThisDevice) {
            return GpuDelegateFactory.Options().apply {
                isPrecisionLossAllowed =
                    allowFp16PrecisionForFp32

                setQuantizedModelsAllowed(allowQuantizedModels)

                inferencePreference = when(inferencePreferenceType){
                    TFLiteInferencePreference.SUSTAINED_SPEED -> GpuDelegateFactory.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED
                    TFLiteInferencePreference.FAST_SINGLE_ANSWER -> GpuDelegateFactory.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER
                    TFLiteInferencePreference.PLATFORM_DEFAULT -> GpuDelegateFactory.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER
                }
            }
        } else
            println("Delegation is not supported on this device, Fall back to CPU.")
        return null
    }

    private fun setNnApiDelegation() : NnApiDelegate.Options? {
        if (compatList.isDelegateSupportedOnThisDevice) {
            return NnApiDelegate.Options().apply {
                allowFp16 = allowFp16PrecisionForFp32
            }
        } else
            println("Delegation is not supported on this device, Fall back to CPU.")
        return null
    }

}
