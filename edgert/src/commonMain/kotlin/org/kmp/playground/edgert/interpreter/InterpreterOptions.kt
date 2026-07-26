package org.kmp.playground.edgert.interpreter

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

enum class TFLiteInferencePreference {
    SUSTAINED_SPEED,
    FAST_SINGLE_ANSWER,
    PLATFORM_DEFAULT
}

expect class InterpreterOptions(
    numThreads: Int = 4,
    delegateType: DelegateType = DelegateType.CPU,
    inferencePreferenceType: TFLiteInferencePreference = TFLiteInferencePreference.PLATFORM_DEFAULT,
    allowQuantizedModels: Boolean = true,
    allowFp16PrecisionForFp32: Boolean = false,
    runtime: RuntimeType = RuntimeType.TFLITE,
    allowBufferHandleOutput: Boolean = true,
) {
    val numThreads: Int
    val delegateType: DelegateType
    val inferencePreferenceType: TFLiteInferencePreference
    val allowQuantizedModels: Boolean
    val allowFp16PrecisionForFp32: Boolean
    val runtime: RuntimeType
    val allowBufferHandleOutput: Boolean
}





