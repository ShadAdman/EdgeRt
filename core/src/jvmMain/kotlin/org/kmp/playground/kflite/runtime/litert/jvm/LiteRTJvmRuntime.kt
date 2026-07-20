package org.kmp.playground.kflite.runtime.litert.jvm

import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

class LiteRTJvmRuntime(
    modelSource: ModelSource,
    options: InterpreterOptions
) {
    private val arena = Arena.ofShared()
    private var env: MemorySegment = MemorySegment.NULL
    private var model: MemorySegment = MemorySegment.NULL
    private var compiledModel: MemorySegment = MemorySegment.NULL

    companion object {
        private val linker = Linker.nativeLinker()
        private val lookup: SymbolLookup by lazy {
            val libName = getLibName()
            val resourcePath = getResourcePath(libName)
            val tempFile = loadLibrary(resourcePath, libName)
            SymbolLookup.libraryLookup(tempFile, Arena.global())
        }

        private fun getLibName(): String {
            val os = System.getProperty("os.name").lowercase(Locale.ENGLISH)
            return when {
                os.contains("win") -> "LiteRt.dll"
                os.contains("mac") -> "libLiteRt.dylib"
                else -> "libLiteRt.so"
            }
        }

        private fun getResourcePath(libName: String): String {
            val os = System.getProperty("os.name").lowercase(Locale.ENGLISH)
            val arch = System.getProperty("os.arch").lowercase(Locale.ENGLISH)
            val platformDir = when {
                os.contains("win") -> "win-x64"
                os.contains("mac") -> if (arch.contains("aarch64")) "mac-arm64" else "mac-x64"
                else -> "linux-x64"
            }
            return "/$platformDir/$libName"
        }

        private fun loadLibrary(resourcePath: String, libName: String): Path {
            val inputStream = LiteRTJvmRuntime::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Cannot find library resource: $resourcePath")
            
            val tempDir = Files.createTempDirectory("litert_native")
            val tempFile = tempDir.resolve(libName)
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING)
            
            // Register for deletion on exit
            tempFile.toFile().deleteOnExit()
            tempDir.toFile().deleteOnExit()
            
            return tempFile
        }

        private fun findFunction(name: String, desc: FunctionDescriptor): MethodHandle {
            return lookup.find(name).map { linker.downcallHandle(it, desc) }
                .orElseThrow { NoSuchElementException("Symbol not found: $name") }
        }

        // Define function handles
        private val LiteRtCreateEnvironment by lazy { findFunction("LiteRtCreateEnvironment", 
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)) }
        
        private val LiteRtCreateModelFromFile by lazy { findFunction("LiteRtCreateModelFromFile",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)) }

        private val LiteRtCreateModelFromBuffer by lazy { findFunction("LiteRtCreateModelFromBuffer",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)) }

        private val LiteRtCreateCompiledModel by lazy { findFunction("LiteRtCreateCompiledModel",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)) }

        private val LiteRtGetNumModelSignatures by lazy { findFunction("LiteRtGetNumModelSignatures",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)) }

        private val LiteRtGetModelSignature by lazy { findFunction("LiteRtGetModelSignature",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)) }

        private val LiteRtGetNumSignatureInputs by lazy { findFunction("LiteRtGetNumSignatureInputs",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)) }

        private val LiteRtGetNumSignatureOutputs by lazy { findFunction("LiteRtGetNumSignatureOutputs",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)) }
        
        private val LiteRtDestroyCompiledModel by lazy { findFunction("LiteRtDestroyCompiledModel",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)) }
        
        private val LiteRtDestroyModel by lazy { findFunction("LiteRtDestroyModel",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)) }
        
        private val LiteRtDestroyEnvironment by lazy { findFunction("LiteRtDestroyEnvironment",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)) }
    }

    init {
        val envPtr = arena.allocate(ValueLayout.ADDRESS)
        println("LiteRT: Creating environment...")
        (LiteRtCreateEnvironment.invokeExact(0, MemorySegment.NULL, envPtr) as Int).checkStatus()
        env = envPtr.get(ValueLayout.ADDRESS, 0)
        println("LiteRT: Environment created at $env")

        val modelPtr = arena.allocate(ValueLayout.ADDRESS)
        when (modelSource) {
            is FileSource -> {
                println("LiteRT: Loading model from file: ${modelSource.path}")
                val pathStr = arena.allocateFrom(modelSource.path)
                (LiteRtCreateModelFromFile.invokeExact(env, pathStr, modelPtr) as Int).checkStatus()
            }
            is ByteArraySource -> {
                println("LiteRT: Loading model from bytes (${modelSource.bytes.size})...")
                val buffer = arena.allocate(modelSource.bytes.size.toLong(), 16)
                MemorySegment.copy(MemorySegment.ofArray(modelSource.bytes), 0, buffer, 0, modelSource.bytes.size.toLong())
                (LiteRtCreateModelFromBuffer.invokeExact(env, buffer, modelSource.bytes.size.toLong(), modelPtr) as Int).checkStatus()
            }
            is ResourceSource, is AssetSource -> {
                val rawPath = if (modelSource is ResourceSource) modelSource.path else (modelSource as AssetSource).path
                val path = if (rawPath.startsWith("/")) rawPath else "/$rawPath"
                println("LiteRT: Loading model from resource: $path")
                
                val modelBytes = LiteRTJvmRuntime::class.java.getResourceAsStream(path)?.readAllBytes()
                    ?: throw IllegalArgumentException("Model source not found in resources: $path")
                
                val tempModelFile = Files.createTempFile("litert_model", ".tflite")
                Files.write(tempModelFile, modelBytes)
                tempModelFile.toFile().deleteOnExit()
                
                println("LiteRT: Extracted to temp file: ${tempModelFile.toAbsolutePath()}")
                val pathStr = arena.allocateFrom(tempModelFile.toAbsolutePath().toString())
                (LiteRtCreateModelFromFile.invokeExact(env, pathStr, modelPtr) as Int).checkStatus()
            }
        }
        model = modelPtr.get(ValueLayout.ADDRESS, 0)
        println("LiteRT: Model loaded at $model")

        val compiledModelPtr = arena.allocate(ValueLayout.ADDRESS)
        println("LiteRT: Compiling model...")
        // Passing NULL for options to use defaults
        val status = LiteRtCreateCompiledModel.invokeExact(env, model, MemorySegment.NULL, compiledModelPtr) as Int
        if (status != 0) {
             println("LiteRT: Compilation FAILED with status $status")
             status.checkStatus()
        }
        compiledModel = compiledModelPtr.get(ValueLayout.ADDRESS, 0)
        println("LiteRT: Model compiled successfully at $compiledModel")
    }

    val inputTensorCount: Int
        get() {
            val signature = getSignature() ?: return 0
            val countPtr = arena.allocate(ValueLayout.JAVA_LONG)
            (LiteRtGetNumSignatureInputs.invokeExact(signature, countPtr) as Int).checkStatus()
            return countPtr.get(ValueLayout.JAVA_LONG, 0).toInt()
        }

    val outputTensorCount: Int
        get() {
            val signature = getSignature() ?: return 0
            val countPtr = arena.allocate(ValueLayout.JAVA_LONG)
            (LiteRtGetNumSignatureOutputs.invokeExact(signature, countPtr) as Int).checkStatus()
            return countPtr.get(ValueLayout.JAVA_LONG, 0).toInt()
        }

    private fun getSignature(index: Int = 0): MemorySegment? {
        val countPtr = arena.allocate(ValueLayout.JAVA_LONG)
        (LiteRtGetNumModelSignatures.invokeExact(model, countPtr) as Int).checkStatus()
        val count = countPtr.get(ValueLayout.JAVA_LONG, 0).toInt()
        if (count <= index) return null
        
        val signaturePtr = arena.allocate(ValueLayout.ADDRESS)
        (LiteRtGetModelSignature.invokeExact(model, index.toLong(), signaturePtr) as Int).checkStatus()
        return signaturePtr.get(ValueLayout.ADDRESS, 0)
    }

    fun getInputTensor(index: Int): Tensor {
        return Tensor(object : RuntimeTensor {
            override val dataType = TensorDataType.FLOAT32
            override val name = "input_$index"
            override val shape = intArrayOf(1, 224, 224, 3)
        })
    }

    fun getOutputTensor(index: Int): Tensor {
        return Tensor(object : RuntimeTensor {
            override val dataType = TensorDataType.FLOAT32
            override val name = "output_$index"
            override val shape = intArrayOf(1, 1000)
        })
    }

    fun resizeInput(index: Int, shape: IntArray) {
    }

    fun run(inputs: List<Any>, outputs: Map<Int, Any>) {
    }

    fun warmUp(config: WarmUpConfig) {
        val inputTensors = (0 until inputTensorCount).map { getInputTensor(it) }
        val outputTensors = (0 until outputTensorCount).map { getOutputTensor(it) }

        repeat(config.iterations) {
            val inputs = inputTensors.mapIndexed { index, tensor ->
                config.inputProvider.createInput(tensor, index)
            }
            val outputs = outputTensors.mapIndexed { index, tensor ->
                index to config.inputProvider.createOutput(tensor, index)
            }.toMap()

            run(inputs, outputs)
        }

        if (config.closeInterpreter) {
            close()
        }
    }

    fun wakeUp() {
    }

    fun getMetadata(): ModelMetadata {
        return ModelMetadata(null, null, null, null, null, null, emptyList(), emptyList())
    }

    fun close() {
        LiteRtDestroyCompiledModel.invokeExact(compiledModel)
        LiteRtDestroyModel.invokeExact(model)
        LiteRtDestroyEnvironment.invokeExact(env)
        arena.close()
    }
}

private fun Int.checkStatus() {
    if (this != 0) {
        throw RuntimeException("LiteRT error: $this")
    }
}
