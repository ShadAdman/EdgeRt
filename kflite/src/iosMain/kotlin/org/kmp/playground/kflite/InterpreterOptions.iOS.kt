package org.kmp.playground.kflite

import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegate
import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegateEnabledDevices
import cocoapods.TFLTensorFlowLite.TFLCoreMLDelegateOptions
import cocoapods.TFLTensorFlowLite.TFLInterpreterOptions
import cocoapods.TFLTensorFlowLite.TFLMetalDelegate
import cocoapods.TFLTensorFlowLite.TFLMetalDelegateOptions
import kotlinx.cinterop.ExperimentalForeignApi

actual class InterpreterOptions actual constructor(
    numThreads: Int,
    delegateType: DelegateType,
    allowFp16PrecisionForFp32: Boolean
) {

    @OptIn(ExperimentalForeignApi::class)
    internal val tflInterpreterOptions = TFLInterpreterOptions().apply {
        setNumberOfThreads(numThreads.toULong())

        when (delegateType) {
            DelegateType.CPU -> {
            }

            DelegateType.GPU_METAL -> TFLMetalDelegateOptions().apply {
                precisionLossAllowed = allowFp16PrecisionForFp32
                //TODO can be a feature update for consumer
                // quantizationEnabled

                TFLMetalDelegate(options = this)
            }

            DelegateType.NNAPI_COREML -> TFLCoreMLDelegateOptions().apply {
                // support all devices. even emulators
                enabledDevices =
                    TFLCoreMLDelegateEnabledDevices.TFLCoreMLDelegateEnabledDevicesAll

                TFLCoreMLDelegate(this)
            }
        }

    }
}