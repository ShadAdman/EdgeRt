package org.kmp.playground.kflite


/**
 * Default is CPU.
 * GPU_METAL delegate: For Android it will be GPU and for iOS this will use Metal
 * NNAPI_COREML delegate: For iOS this will use CoreML and for Android it will use NNAPI
 */
enum class DelegateType {
    CPU,
    GPU_METAL,
    NNAPI_COREML
}

enum class TFLiteInferencePreference {
    /**
     * Optimizes for low latency and high throughput for continuous/live data (e.g., camera feed).
     * This typically means a more aggressive use of device resources.
     */
    SUSTAINED_SPEED,

    /**
     * Optimizes for the absolute fastest time to get the first result (single run).
     * This is suitable for a single, immediate inference task.
     */
    FAST_SINGLE_ANSWER,

    /**
     * Use the default setting provided by the underlying platform (recommended if unsure).
     */
    PLATFORM_DEFAULT
}

/**
 * Encapsulates settings for configuring an interpreters.
 * @param numThreads The number of threads to be used for ops that support multi-threading. Only for CPU
 * @param delegateType The type of delegate to be used for hardware acceleration.
 * @param inferencePreferenceType The preference for inference speed and accuracy.
 * @param allowQuantizedModels Whether to allow inference with quantized models.
 * @param allowFp16PrecisionForFp32 Whether to allow inference with float16 precision for FP32 models.
 */
expect class InterpreterOptions(
    numThreads: Int = 4,
    delegateType: DelegateType = DelegateType.CPU,
    inferencePreferenceType: TFLiteInferencePreference = TFLiteInferencePreference.PLATFORM_DEFAULT,
    allowQuantizedModels: Boolean = true,
    allowFp16PrecisionForFp32: Boolean = false,
)