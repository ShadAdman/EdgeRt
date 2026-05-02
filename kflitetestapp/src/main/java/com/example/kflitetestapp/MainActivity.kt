package com.example.kflitetestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import org.kmp.playground.kflite.KfliteClass

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var modelByteArray: ByteArray? = null
        val modelName = "celsius_to_fahrenheit.tflite"
        assets.open(modelName).use { inputStream ->
            modelByteArray = inputStream.readBytes()
        }

        modelByteArray ?: return

        val tfClass = KfliteClass(modelByteArray)
        val output = mapOf(0 to arrayOf(FloatArray(1)))
        val input = listOf(arrayOf(floatArrayOf(100f)))
        tfClass.run(input, output)
        tfClass.close()

        println("model result: ${output[0]?.get(0).contentToString()}")

        setContent {
            Text(text = "testApp")
        }
    }
}