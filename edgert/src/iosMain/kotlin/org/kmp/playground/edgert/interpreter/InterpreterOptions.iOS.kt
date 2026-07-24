package org.kmp.playground.edgert.interpreter

import kotlinx.cinterop.ExperimentalForeignApi
import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegate
import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegateEnabledDevices
import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegateOptions
import cocoapods.TFLTensorFlowLite.TFLInterpreterOptions
import cocoapods.TFLTensorFlowLite.TFLMetalDelegate
import cocoapods.TFLTensorFlowLite.TFLMetalDelegateOptions
import cocoapods.TFLTensorFlowLite.TFLMetalDelegateThreadWaitType

actual class InterpreterOptions actual constructor(
    actual val numThreads: Int,
    actual val delegateType: DelegateType,
    actual val inferencePreferenceType: TFLiteInferencePreference,
    actual val allowQuantizedModels: Boolean,
    actual val allowFp16PrecisionForFp32: Boolean,
    actual val runtime: RuntimeType,
    actual val allowBufferHandleOutput: Boolean,
){
    @OptIn(ExperimentalForeignApi::class)
    internal val tflInterpreterOptions = PlatformInterpreterOptions().apply {
        setNumberOfThreads(numThreads.toULong())

        when (delegateType) {
            DelegateType.CPU -> Unit
            DelegateType.GPU_METAL -> setMetalDelegation()
            DelegateType.NNAPI_COREML -> setCoreMLDelegation()
        }

    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setMetalDelegation() = TFLMetalDelegateOptions().apply {
        precisionLossAllowed = allowFp16PrecisionForFp32
        quantizationEnabled = allowQuantizedModels
        waitType = when(inferencePreferenceType){
            TFLiteInferencePreference.SUSTAINED_SPEED -> TFLMetalDelegateThreadWaitType.TFLMetalDelegateThreadWaitTypeAggressive
            TFLiteInferencePreference.FAST_SINGLE_ANSWER -> TFLMetalDelegateThreadWaitType.TFLMetalDelegateThreadWaitTypeActive
            TFLiteInferencePreference.PLATFORM_DEFAULT -> TFLMetalDelegateThreadWaitType.TFLMetalDelegateThreadWaitTypePassive
        }
        TFLMetalDelegate(options = this)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun setCoreMLDelegation() = TFLCoreMLDelegateOptions().apply {
        enabledDevices =
            TFLCoreMLDelegateEnabledDevices.TFLCoreMLDelegateEnabledDevicesAll
        TFLCoreMLDelegate(options = this)
    }
}





