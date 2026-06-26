package org.kmp.playground.kflite.core.model

data class Normalization(
    val originalImageHeight: Float,
    val originalImageWidth: Float,
    val modelImagWidth: Float,
    val modelImageHeight: Float
)

data class Box(
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float
)
