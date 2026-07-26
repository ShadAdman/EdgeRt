package org.kmp.playground.edgert.postprocessing.reshape

/**
 * N-Dimensional Reshaper.
 *
 * reshape multi-dimensional AI model outputs.
 * * CONCEPT: Every model output is fundamentally a flat matrix (grid of numbers) in memory.
 * - Vision (YOLO): Each row is a bounding box/class feature; columns are detected locations.
 * - NLP (Text): Each row is a text token/word; columns are its embedding dimensions.
 * - Segmentation: Each row is a category (e.g., Tooth/Gum); columns are pixel coordinates.
 * This class shifts the row/column perspective by calculating memory index strides.
 *
 * Example: To change [1, 40, 8400] to [1, 8400, 40], permuteAxes = intArrayOf(0, 2, 1)
 */
class ReshapePostProcessing(
    val originalShape: IntArray,
    val targetShape: IntArray
) {
    init {
        require(originalShape.fold(1) { acc, i -> acc * i } == targetShape.fold(1) { acc, i -> acc * i }) {
            "Total elements must match! Cannot reshape ${originalShape.joinToString()} to ${targetShape.joinToString()}"
        }
    }

    fun permute(
        input: Array<Array<FloatArray>>,
        permuteAxes: IntArray
    ): Array<Array<FloatArray>> {

        val flat = input.flatten()

        val result = permute(flat, permuteAxes)

        return result.to3D(targetShape)
    }

    /**
     * Permutes/transposes a flat FloatArray based on a desired axis mapping.
     * Example: To change [1, 40, 8400] to [1, 8400, 40], permuteAxes = intArrayOf(0, 2, 1)
     */
    fun permute(input: FloatArray, permuteAxes: IntArray): FloatArray {
        val output = FloatArray(input.size)
        val rank = originalShape.size

        // Compute strides for the original shape
        val strides = IntArray(rank)
        var currentStride = 1
        for (i in rank - 1 downTo 0) {
            strides[i] = currentStride
            currentStride *= originalShape[i]
        }

        // Compute new shape and new strides after permutation
        val newShape = IntArray(rank) { originalShape[permuteAxes[it]] }
        val newStrides = IntArray(rank)
        currentStride = 1
        for (i in rank - 1 downTo 0) {
            newStrides[i] = currentStride
            currentStride *= newShape[i]
        }

        // Helper tracking array for N-dimensional loop coordinates
        val coords = IntArray(rank)

        for (linearIndex in input.indices) {
            // 1. Convert linear source index to multi-dimensional source coordinates
            var tempIndex = linearIndex
            for (i in 0 until rank) {
                coords[i] = tempIndex / strides[i]
                tempIndex %= strides[i]
            }

            // 2. Map source coordinates to permuted target linear index
            var targetLinearIndex = 0
            for (i in 0 until rank) {
                val origAxis = permuteAxes[i]
                targetLinearIndex += coords[origAxis] * newStrides[i]
            }

            output[targetLinearIndex] = input[linearIndex]
        }

        return output
    }


    private fun Array<Array<FloatArray>>.flatten(): FloatArray {
        val size = sumOf { level1 ->
            level1.sumOf { it.size }
        }

        val output = FloatArray(size)
        var index = 0

        for (a in this) {
            for (b in a) {
                b.copyInto(output, index)
                index += b.size
            }
        }

        return output
    }

    private fun FloatArray.to3D(shape: IntArray): Array<Array<FloatArray>> {
        require(shape.size == 3)

        val (d0, d1, d2) = shape

        var offset = 0

        return Array(d0) {
            Array(d1) {
                FloatArray(d2).also {
                    copyInto(it, 0, offset, offset + d2)
                    offset += d2
                }
            }
        }
    }
}