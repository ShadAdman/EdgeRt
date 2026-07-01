package org.kmp.playground.kflite.doc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.getElementById("compose-receiver") ?: return
    ComposeViewport(body) {
        DocApp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocApp() {
    var currentSection by remember { mutableStateOf(Section.Landing) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column {
                TopAppBar(
                    title = { Text("KFlite Documentation") },
                    actions = {
                        TextButton(onClick = { currentSection = Section.Landing }) { Text("Home") }
                        TextButton(onClick = { currentSection = Section.Kflite }) { Text("kflite") }
                        TextButton(onClick = { currentSection = Section.Preprocessing }) { Text("preprocessing") }
                        TextButton(onClick = { currentSection = Section.Postprocessing }) { Text("postprocessing") }
                        TextButton(onClick = { currentSection = Section.Coldstart }) { Text("coldstart") }
                    }
                )

                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    when (currentSection) {
                        Section.Landing -> LandingPage()
                        Section.Kflite -> ModuleDoc("kflite", "The main runtime and model management module.")
                        Section.Preprocessing -> ModuleDoc("preprocessing", "Utilities for preparing data before inference (Image, etc.).")
                        Section.Postprocessing -> ModuleDoc("postprocessing", "Utilities for processing inference results (NMS, COCO, etc.).")
                        Section.Coldstart -> ModuleDoc("coldstart", "Configurable dry run engine for model warm-up.")
                    }
                }
            }
        }
    }
}

@Composable
fun LandingPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to KFlite", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "A lightweight, modular Kotlin Multiplatform library for TensorFlow Lite and LiteRT.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ModuleDoc(name: String, description: String) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Installation", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            PaddingBox {
                Text("implementation(\"io.github.shadadman:$name:3.4.0\")")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("API Highlights", style = MaterialTheme.typography.titleLarge)
        Text("Placeholder for $name API documentation details...")
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(16.dp)) {
        content()
    }
}

enum class Section {
    Landing, Kflite, Preprocessing, Postprocessing, Coldstart
}
