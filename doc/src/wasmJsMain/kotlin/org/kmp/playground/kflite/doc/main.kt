package org.kmp.playground.kflite.doc

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import kflitelib.doc.generated.resources.Res
import kflitelib.doc.generated.resources.poster
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val body = document.getElementById("compose-receiver") ?: return
    ComposeViewport(body) {
        DocApp()
    }
}

sealed class NavItem(val title: String, val icon: ImageVector) {
    data object Intro : NavItem("Introduction", Icons.Default.Home)
    data object TFLite : NavItem("TFLite Runtime", Icons.Default.Settings)
    data object LiteRT : NavItem("LiteRT Runtime", Icons.Default.Bolt)
    
    sealed class PostProcessing(title: String) : NavItem(title, Icons.Default.Build) {
        data object Reshaping : PostProcessing("Reshaping")
        data object Normalization : PostProcessing("Normalization")
    }

    sealed class PreProcessing(title: String) : NavItem(title, Icons.Default.Image) {
        data object Image : PreProcessing("Image")
    }
}

@Composable
fun DocApp() {
    var currentItem by remember { mutableStateOf<NavItem>(NavItem.Intro) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFE0E0E0),
            primaryContainer = Color(0xFF3700B3),
            onPrimaryContainer = Color.White
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                Surface(
                    modifier = Modifier.width(300.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Sidebar(currentItem) { currentItem = it }
                }

                // Content Area
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 32.dp)) {
                    ContentArea(currentItem)
                }
            }
        }
    }
}

@Composable
fun Sidebar(selectedItem: NavItem, onItemSelected: (NavItem) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(vertical = 24.dp)) {
        Text(
            "KFlite Documentation",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.DarkGray)

        LazySidebarContent(selectedItem, onItemSelected)
    }
}

@Composable
fun LazySidebarContent(selectedItem: NavItem, onItemSelected: (NavItem) -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SidebarItem(NavItem.Intro, selectedItem == NavItem.Intro) { onItemSelected(NavItem.Intro) }
        
        SectionLabel("Runtimes")
        SidebarItem(NavItem.TFLite, selectedItem == NavItem.TFLite) { onItemSelected(NavItem.TFLite) }
        SidebarItem(NavItem.LiteRT, selectedItem == NavItem.LiteRT) { onItemSelected(NavItem.LiteRT) }

        SectionLabel("Postprocessing")
        SidebarItem(NavItem.PostProcessing.Reshaping, selectedItem == NavItem.PostProcessing.Reshaping, indent = 16) { 
            onItemSelected(NavItem.PostProcessing.Reshaping) 
        }
        SidebarItem(NavItem.PostProcessing.Normalization, selectedItem == NavItem.PostProcessing.Normalization, indent = 16) { 
            onItemSelected(NavItem.PostProcessing.Normalization) 
        }

        SectionLabel("Preprocessing")
        SidebarItem(NavItem.PreProcessing.Image, selectedItem == NavItem.PreProcessing.Image, indent = 16) { 
            onItemSelected(NavItem.PreProcessing.Image) 
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SidebarItem(item: NavItem, isSelected: Boolean, indent: Int = 0, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).padding(start = indent.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ContentArea(item: NavItem) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when (item) {
            is NavItem.Intro -> IntroPage()
            is NavItem.TFLite -> DetailPage(
                title = "TFLite Runtime",
                what = "The TensorFlow Lite (TFLite) runtime is the established standard for running machine learning models on edge devices. It supports a vast library of operators and is highly optimized for mobile CPU and GPU execution.",
                whenToUse = "Use the TFLite runtime when you need broad compatibility with existing models, or when you require specific hardware acceleration delegates that are mature in the TFLite ecosystem.",
                howToUse = "Configure the `InterpreterOptions` with `RuntimeType.TFLITE` during initialization:\n\n```kotlin\nval options = InterpreterOptions(runtime = RuntimeType.TFLITE)\nKflite.init(modelSource, options)\n```"
            )
            is NavItem.LiteRT -> DetailPage(
                title = "LiteRT Runtime",
                what = "LiteRT (formerly known as TensorFlow Lite Runtime) is Google's next-generation runtime for AI at the edge. It focuses on reduced binary size and improved performance through a modernized architecture.",
                whenToUse = "Choose LiteRT for new projects where you want the latest performance improvements and a more streamlined runtime experience on Android and beyond.",
                howToUse = "Select `RuntimeType.LITERT` in your `InterpreterOptions`:\n\n```kotlin\nval options = InterpreterOptions(runtime = RuntimeType.LITERT)\nKflite.init(modelSource, options)\n```"
            )
            is NavItem.PostProcessing.Reshaping -> DetailPage(
                title = "Postprocessing: Reshaping",
                what = "Reshaping is a utility that allows you to transform the multi-dimensional output arrays of a model into a more convenient shape without changing the data itself. It's primarily used to permute axes (transposing) to match the expected format of subsequent logic.",
                whenToUse = "Use this when a model (like YOLOv8) outputs tensors in a 'transposed' format (e.g., [1, 84, 8400] instead of [1, 8400, 84]) which makes iterating through detections inefficient.",
                howToUse = "Use the `ReshapePostProcessing` class to define the transformation and call `permute`:\n\n```kotlin\nval reshaper = ReshapePostProcessing(\n    originalShape = intArrayOf(1, 84, 8400),\n    targetShape = intArrayOf(1, 8400, 84)\n)\nval results = reshaper.permute(rawOutput, intArrayOf(0, 2, 1))\n```"
            )
            is NavItem.PostProcessing.Normalization -> DetailPage(
                title = "Postprocessing: Normalization",
                what = "Normalization in post-processing refers to the process of rescaling relative bounding box coordinates (often in the range [0, 1]) produced by a model back to the pixel coordinates of the original high-resolution image.",
                whenToUse = "You need this whenever you want to draw detection results (like boxes or labels) on an image that has different dimensions than the input image used for inference.",
                howToUse = "Create a `Normalization` instance with the dimensions and use the extension methods for your model type:\n\n```kotlin\nval norm = Normalization(\n    originalImageHeight = 1080f,\n    originalImageWidth = 1920f,\n    modelImageHeight = 640f,\n    modelImageWidth = 640f\n)\n\n// Get original pixel coordinates for a YOLO detection\nval originalBox = norm.YOLO(cx, cy, w, h)\n```"
            )
            is NavItem.PreProcessing.Image -> DetailPage(
                title = "Preprocessing: Image",
                what = "Image preprocessing involves resizing, cropping, and converting image pixels into a normalized byte buffer format that a machine learning model can ingest as input.",
                whenToUse = "Use this before every image-based inference. It ensures the input data matches the model's expected dimensions, color space, and data type (Float32 or Uint8).",
                howToUse = "Use the `toScaledByteBuffer` extension on an `ImageBitmap` or `ByteArray`:\n\n```kotlin\nval inputBuffer = bitmap.toScaledByteBuffer(\n    inputWidth = 640,\n    inputHeight = 640,\n    inputAllocateSize = 640 * 640 * 3 * 4, // 4 bytes for Float32\n    normalize = true // Scales pixels to [0, 1]\n)\n```"
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun IntroPage() {
    Column {
        Text("KFlite", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text(
            "Kotlin Multiplatform ML Runtime",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Image(
                painter = painterResource(Res.drawable.poster),
                contentDescription = "KFlite Poster",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        SectionHeader("About")
        Text(
            "`kflite` runs ML models (TensorFlow, PyTorch, JAX) on mobile devices using Kotlin Multiplatform. It abstracts platform-specific complexities (JNI on Android, C-API on iOS) behind a unified, high-performance API.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        SectionHeader("Features")
        BulletPoint("Native performance with unified KMP API")
        BulletPoint("Switch runtimes between TFLite and LiteRT")
        BulletPoint("Built-in Preprocessing (Image scaling/normalization)")
        BulletPoint("Built-in Postprocessing (Reshaping, Box normalization, NMS)")
        BulletPoint("Hardware acceleration support (GPU, NNAPI, Metal, CoreML)")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SectionHeader("Installation")
        DetailSection("Gradle", "implementation(\"io.github.shadadman:kflite:3.4.0\")", isCode = true)
    }
}

@Composable
fun DetailPage(title: String, what: String, whenToUse: String, howToUse: String) {
    Column {
        Text(title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(48.dp))

        DetailSection("What is this?", what)
        DetailSection("When you need this?", whenToUse)
        DetailSection("How to use it?", howToUse, isCode = true)
    }
}

@Composable
fun DetailSection(header: String, content: String, isCode: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 40.dp)) {
        Text(
            header,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (isCode) {
            Surface(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Text(
                    content,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = Color(0xFFB0BEC5)
                )
            }
        } else {
            Text(
                content,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
        Text("•", modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}
