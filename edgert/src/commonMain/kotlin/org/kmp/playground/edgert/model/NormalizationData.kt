package org.kmp.playground.edgert.model

import org.kmp.playground.edgert.delegation.*
import org.kmp.playground.edgert.interpreter.*
import org.kmp.playground.edgert.edgert.*
import org.kmp.playground.edgert.model.*
import org.kmp.playground.edgert.tensor.*

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








