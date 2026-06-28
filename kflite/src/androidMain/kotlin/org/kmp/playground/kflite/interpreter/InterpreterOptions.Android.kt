package org.kmp.playground.kflite.interpreter

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

actual class InterpreterOptions actual constructor(
    actual val numThreads: Int,
    actual val delegateType: DelegateType,
    actual val inferencePreferenceType: TFLiteInferencePreference,
    actual val allowQuantizedModels: Boolean,
    actual val allowFp16PrecisionForFp32: Boolean,
    actual val runtime: RuntimeType,
    actual val allowBufferHandleOutput: Boolean,
)



