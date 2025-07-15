package org.kmp.playground.kflite
/**
 * Converts various bounding box formats (Pascal VOC, COCO, YOLO, etc.)
 * into a normalized center-based format (cx, cy, w, h),
 * then rescales them to pixel coordinates relative to the original image size.
 *
 * Also provides a utility to convert a center-based Box into a Path rectangle
 * that can be drawn on a canvas.
 *
 * Normalization Usage:
 * val norm = Normalization(image_height, image_width, modelimg_width, modelimg_height)
 * val box = norm.pascalVOC(x_min, y_min, x_max, y_max)
 *
 *
 * @sample org.kmp.playground.kflite.sample.addBoxRect
 * @sample org.kmp.playground.kflite.sample.PathUsage
 *
 *
 * Supported Formats for Normalization:
 * - pascalVOC(x_min, y_min, x_max, y_max)
 * - coco(x, y, width, height)
 * - yolo(cx, cy, width, height)
 * - tfObjectDetection(top, left, bottom, right)
 * - tfRecordVariant(x_min, y_min, x_max, y_max)
 *
 * Output:
 * Box object in pixel space: (center_x, center_y, width, height)
 *
 * Drawing:
 * Converts center-based (cx, cy, w, h) to (left, top, right, bottom)
 * and builds a closed rectangular Path for rendering on Canvas.
 */


fun Normalization.PascalVOC(x_min: Float, y_min: Float, x_max: Float, y_max: Float): Box{
    val w = x_max - x_min
    val h = y_max - y_min
    val center_x = (x_max + x_min) / 2
    val center_y = (y_max + y_min) / 2

    return ResizeBox(
        Box(
            center_x / modelimg_width,
            center_y / modelimg_height,
            w / modelimg_width,
            h / modelimg_height
        ),
        image_width,
        image_height
    )
}


fun Normalization.COCO(x: Float, y: Float, width: Float, height: Float): Box {
    val center_x = x + width / 2
    val center_y = y + height / 2
    return ResizeBox(
        Box(
            center_x / modelimg_width,
            center_y / modelimg_height,
            width / modelimg_width,
            height / modelimg_height
        ),
        image_width,
        image_height
    )
}

fun Normalization.YOLO(center_x: Float, center_y: Float, width: Float, height: Float): Box {
    return ResizeBox(
        Box(
            center_x / modelimg_width,
            center_y / modelimg_height,
            width / modelimg_width,
            height / modelimg_height
        ),
        image_width,
        image_height)
}

fun Normalization.TFObjectDetection(top: Float, left: Float, bottom: Float, right: Float): Box {
    val w = right - left
    val h = bottom - top
    val center_x = (right + left) / 2
    val center_y = (bottom + top) / 2

    return ResizeBox(
        Box(
            center_x / modelimg_width,
            center_y / modelimg_height,
            w / modelimg_width,
            h / modelimg_height
        ),
        image_width,
        image_height)
}

fun Normalization.TFRecordVariant(x_min: Float, y_min: Float, x_max: Float, y_max: Float): Box {
    val w = x_max - x_min
    val h = y_max - y_min
    val center_x = (x_max + x_min) / 2
    val center_y = (y_max + y_min) / 2

    return ResizeBox(
        Box(
            center_x / modelimg_width,
            center_y / modelimg_height,
            w / modelimg_width,
            h / modelimg_height
        ),
        image_width,
        image_height)
}

fun ResizeBox(box: Box, origW: Float, origH:Float): Box{
    return Box(
        box.cx * origW,
        box.cy * origH,
        box.w * origW,
        box.h * origH
    )
}