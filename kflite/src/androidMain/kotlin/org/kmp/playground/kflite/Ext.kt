package org.kmp.playground.kflite

import android.content.Context
import org.tensorflow.lite.DataType
import java.io.File

internal fun PlatformTensor.toTensor() = Tensor(this)

internal fun TensorDataType.toTensorDataType() = when(this){
    TensorDataType.FLOAT32 -> DataType.FLOAT32
    TensorDataType.INT32 -> DataType.INT32
    TensorDataType.UINT8 -> DataType.UINT8
    TensorDataType.INT64 -> DataType.INT64
}

internal fun ByteArray.writeToTempFile(context: Context, prefix: String = "model", suffix: String = ".tflite"): File {
    val tempFile = File.createTempFile(prefix, suffix, context.cacheDir)
    tempFile.outputStream().use { output ->
        output.write(this)
        output.flush()
        output.fd.sync() // Ensure all bytes are flushed to disk
    }
    return tempFile
}
