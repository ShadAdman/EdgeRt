package org.kmp.playground.kflite

enum class DelegateType {
    CPU,
    GPU_METAL,
    NNAPI_COREML
}

expect class InterpreterOptions(
    numThreads: Int = 4,
    delegateType: DelegateType = DelegateType.CPU,
    allowFp16PrecisionForFp32: Boolean = false,
)