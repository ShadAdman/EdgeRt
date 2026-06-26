package org.kmp.playground.kflite.core.interpreter

import org.kmp.playground.kflite.core.delegate.DelegateType
import org.kmp.playground.kflite.core.runtime.RuntimeType

actual class InterpreterOptions actual constructor(
    actual val numThreads: Int,
    actual val delegateType: DelegateType,
    actual val inferencePreferenceType: TFLiteInferencePreference,
    actual val allowQuantizedModels: Boolean,
    actual val allowFp16PrecisionForFp32: Boolean,
    actual val runtime: RuntimeType,
    actual val allowBufferHandleOutput: Boolean,
)
