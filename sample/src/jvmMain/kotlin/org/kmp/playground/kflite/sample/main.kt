package org.kmp.playground.kflite.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KFlite Sample (JVM)",
    ) {
        App()
    }
}
