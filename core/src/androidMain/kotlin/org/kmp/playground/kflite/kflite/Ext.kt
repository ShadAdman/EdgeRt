package org.kmp.playground.kflite.kflite

import org.kmp.playground.kflite.delegation.*
import org.kmp.playground.kflite.interpreter.*
import org.kmp.playground.kflite.kflite.*
import org.kmp.playground.kflite.model.*
import org.kmp.playground.kflite.tensor.*

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



