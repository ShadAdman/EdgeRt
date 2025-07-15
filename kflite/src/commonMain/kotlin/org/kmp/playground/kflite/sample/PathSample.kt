package org.kmp.playground.kflite.sample

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import org.kmp.playground.kflite.Normalization




fun Path.addBoxRect(box: Box) {
    //Drawing Usage (Jetpack Compose):
    val left = box.cx - box.w / 2
    val top = box.cy - box.h / 2
    val right = box.cx + box.w / 2
    val bottom = box.cy + box.h / 2

    reset()
    moveTo(left, top)
    lineTo(right, top)
    lineTo(right, bottom)
    lineTo(left, bottom)
    close()
}

@Composable
fun PathUsage(){
    val path = Path().apply { addBoxRect(Normalization(
        image_height = 1080f, //Original input height
        image_width = 2010f, // Original input width
        modelimg_width = 680f, //Model input width
        modelimg_height = 680f //Model input height
    ).YOLO(
        center_x = 20f, //CenterX of Model Output From The Model
        center_y = 20f,//CenterY of Model Output From The Model
        width = 100f,  //Width of Model Output From The Model
        height = 120f //Height of Model Output From The Model
    )) }
    Canvas(modifier= Modifier){
        drawPath(path = path, color = Color.Red, style = Stroke(width = 2f))
    }
}


data class Box(val cx: Float, val cy: Float, val w: Float, val h: Float)
