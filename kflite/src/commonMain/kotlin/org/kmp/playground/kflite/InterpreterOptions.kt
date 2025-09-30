package org.kmp.playground.kflite


/**
 * Default is CPU delegate.
 * GPU_METAL delegate: For Android it will be GPU and for iOS this will use Metal
 * NNAPI_COREML delegate: For iOS this will use CoreML and for Android it will use NNAPI
 */
enum class DelegateType {
    CPU,
    GPU_METAL,
    NNAPI_COREML
}

/**
 * Encapsulates settings for configuring an interpreter.
 * @param numThreads The number of threads to be used for ops that support multi-threading. Only for CPU
 * @param delegateType The type of delegate to be used for hardware acceleration.
 * @param allowFp16PrecisionForFp32 Whether to allow inference with float16 precision for FP32 models.
 */
expect class InterpreterOptions(
    numThreads: Int = 4,
    delegateType: DelegateType = DelegateType.CPU,
    allowFp16PrecisionForFp32: Boolean = false,
)