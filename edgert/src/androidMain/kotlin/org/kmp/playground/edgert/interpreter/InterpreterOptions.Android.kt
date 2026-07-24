package org.kmp.playground.edgert.interpreter

import com.google.ai.edge.litert.Accelerator
import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

actual class InterpreterOptions actual constructor(
    actual val numThreads: Int,
    actual val delegateType: DelegateType,
    actual val inferencePreferenceType: TFLiteInferencePreference,
    actual val allowQuantizedModels: Boolean,
    actual val allowFp16PrecisionForFp32: Boolean,
    actual val runtime: RuntimeType,
    actual val allowBufferHandleOutput: Boolean,
){

//    val compatList = CompatibilityList()

    internal val tensorFlowInterpreterOptions = PlatformTFLiteInterpreterOptions()
        .setNumThreads(numThreads)
//        .setAllowBufferHandleOutput(allowBufferHandleOutput)
//        .apply {
//            when (delegateType) {
//                DelegateType.CPU -> Unit
//                DelegateType.GPU_METAL -> {
//                    setGpuDelegation()?.let {
//                        addDelegate(GpuDelegateFactory(it).create(RuntimeFlavor.APPLICATION))
//                    }
//                }
//
//                DelegateType.NNAPI_COREML -> {
//                    setNnApiDelegation()?.let {
//                        addDelegate(NnApiDelegate(it))
//                    }
//                }
//            }
//        }

    internal val liteRTInterpreterOptions = PlatformLiteRTInterpreterOptions(liteRTAccelerator)


    internal val liteRTAccelerator: Accelerator
        get() = when (delegateType) {
            DelegateType.CPU -> Accelerator.CPU
            DelegateType.GPU_METAL -> Accelerator.GPU
            DelegateType.NNAPI_COREML -> Accelerator.NPU
        }

//    private fun setGpuDelegation() : GpuDelegateFactory.Options? {
//        if (compatList.isDelegateSupportedOnThisDevice) {
//            return GpuDelegateFactory.Options().apply {
//                isPrecisionLossAllowed =
//                    allowFp16PrecisionForFp32
//
//                setQuantizedModelsAllowed(allowQuantizedModels)
//
//                inferencePreference = when(inferencePreferenceType){
//                    TFLiteInferencePreference.SUSTAINED_SPEED -> GpuDelegateFactory.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED
//                    TFLiteInferencePreference.FAST_SINGLE_ANSWER -> GpuDelegateFactory.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER
//                    TFLiteInferencePreference.PLATFORM_DEFAULT -> GpuDelegateFactory.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER
//                }
//            }
//        } else
//            println("Delegation is not supported on this device, Fall back to CPU.")
//        return null
//    }
//
//    private fun setNnApiDelegation() : NnApiDelegate.Options? {
//        if (compatList.isDelegateSupportedOnThisDevice) {
//            return NnApiDelegate.Options().apply {
//                allowFp16 = allowFp16PrecisionForFp32
//            }
//        } else
//            println("Delegation is not supported on this device, Fall back to CPU.")
//        return null
//    }

}





