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
data class Normalization(val originalImageHeight: Float, val originalImageWidth: Float, val modelImagWidth: Float, val modelImageHeight: Float)

data class Box(val cx: Float, val cy: Float, val w: Float, val h: Float)