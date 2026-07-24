package org.kmp.playground.edgert.edgert

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

import android.content.Context
import org.tensorflow.lite.DataType
import java.io.File

fun TensorDataType.toTensorDataType() = when(this){
    TensorDataType.FLOAT32 -> DataType.FLOAT32
    TensorDataType.INT32 -> DataType.INT32
    TensorDataType.UINT8 -> DataType.UINT8
    TensorDataType.INT64 -> DataType.INT64
}

fun ByteArray.writeToTempFile(context: Context, prefix: String = "model", suffix: String = ".tflite"): File {
    val tempFile = File.createTempFile(prefix, suffix, context.cacheDir)
    tempFile.outputStream().use { output ->
        output.write(this)
        output.flush()
        output.fd.sync()
    }
    return tempFile
}





