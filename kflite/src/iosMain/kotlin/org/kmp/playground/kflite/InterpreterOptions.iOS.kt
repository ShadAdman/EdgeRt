package org.kmp.playground.kflite

import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegate
import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegateEnabledDevices
import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegateOptions
import cocoapods.TFLTensorFlowLite.TFLInterpreterOptions
import cocoapods.TFLTensorFlowLite.TFLMetalDelegate
import cocoapods.TFLTensorFlowLite.TFLMetalDelegateOptions
import cocoapods.TFLTensorFlowLite.TFLMetalDelegateThreadWaitType
import kotlinx.cinterop.ExperimentalForeignApi

actual class InterpreterOptions actual constructor(
    numThreads: Int,
    val delegateType: DelegateType,
    val inferencePreferenceType: TFLiteInferencePreference,
    val allowQuantizedModels: Boolean,
    val allowFp16PrecisionForFp32: Boolean
) {

    @OptIn(ExperimentalForeignApi::class)
    internal val tflInterpreterOptions = TFLInterpreterOptions().apply {
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