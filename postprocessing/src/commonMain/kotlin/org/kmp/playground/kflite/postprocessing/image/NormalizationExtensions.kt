package org.kmp.playground.kflite.postprocessing.image

import org.kmp.playground.kflite.model.Normalization
import org.kmp.playground.kflite.model.Box

fun Normalization.PascalVOC(x_min: Float, y_min: Float, x_max: Float, y_max: Float): Box{
    val w = x_max - x_min
    val h = y_max - y_min
    val center_x = (x_max + x_min) / 2
    val center_y = (y_max + y_min) / 2

    return resizeBox(
        Box(
            center_x / modelImagWidth,
            center_y / modelImageHeight,
            w / modelImagWidth,
            h / modelImageHeight
        ),
        originalImageWidth,
        originalImageHeight
    )
}


fun Normalization.COCO(x: Float, y: Float, width: Float, height: Float): Box {
    val center_x = x + width / 2
    val center_y = y + height / 2
    return resizeBox(
        Box(
            center_x / modelImagWidth,
            center_y / modelImageHeight,
            width / modelImagWidth,
            height / modelImageHeight
        ),
        originalImageWidth,
        originalImageHeight
    )
}

fun Normalization.YOLO(center_x: Float, center_y: Float, width: Float, height: Float): Box {
    return resizeBox(
        Box(
            center_x / modelImagWidth,
            center_y / modelImageHeight,
            width / modelImagWidth,
            height / modelImageHeight
        ),
        originalImageWidth,
        originalImageHeight)
}

fun Normalization.TFObjectDetection(top: Float, left: Float, bottom: Float, right: Float): Box {
    val w = right - left
    val h = bottom - top
    val center_x = (right + left) / 2
    val center_y = (bottom + top) / 2

    return resizeBox(
        Box(
            center_x / modelImagWidth,
            center_y / modelImageHeight,
            w / modelImagWidth,
            h / modelImageHeight
        ),
        originalImageWidth,
        originalImageHeight)
}

fun Normalization.TFRecordVariant(x_min: Float, y_min: Float, x_max: Float, y_max: Float): Box {
    val w = x_max - x_min
    val h = y_max - y_min
    val center_x = (x_max + x_min) / 2
    val center_y = (y_max + y_min) / 2

    return resizeBox(
        Box(
            center_x / modelImagWidth,
            center_y / modelImageHeight,
            w / modelImagWidth,
            h / modelImageHeight
        ),
        originalImageWidth,
        originalImageHeight)
}

private fun resizeBox(box: Box, origW: Float, origH:Float): Box{
    return Box(
        box.cx * origW,
        box.cy * origH,
        box.w * origW,
        box.h * origH
    )
}

