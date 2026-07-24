package org.kmp.playground.edgert.interpreter

import org.kmp.playground.edgert.tensor.Tensor
import org.kmp.playground.edgert.tensor.TensorDataType
import kotlin.random.Random

/**
 * Interface for providing dummy input and output data for dry runs.
 */
interface InputProvider {
    /**
     * Creates dummy input data for the given [tensor] at [index].
     */
    fun createInput(tensor: Tensor, index: Int): Any

    /**
     * Creates a dummy container for output data for the given [tensor] at [index].
     */
    fun createOutput(tensor: Tensor, index: Int): Any
}

/**
 * Default [InputProvider] that creates zero-filled arrays.
 */
object ZeroInputProvider : InputProvider {
    override fun createInput(tensor: Tensor, index: Int): Any = createDummyData(tensor.dataType, tensor.shape)
    override fun createOutput(tensor: Tensor, index: Int): Any = createDummyData(tensor.dataType, tensor.shape)

    internal fun createDummyData(dataType: TensorDataType, shape: IntArray): Any {
        val totalElements = shape.fold(1) { acc, i -> acc * (if (i <= 0) 1 else i) }
        return when (dataType) {
            TensorDataType.FLOAT32 -> FloatArray(totalElements)
            TensorDataType.INT32 -> IntArray(totalElements)
            TensorDataType.UINT8 -> ByteArray(totalElements)
            TensorDataType.INT64 -> LongArray(totalElements)
        }
    }
}

/**
 * [InputProvider] for image models that allows providing specific input data (e.g. from an image).
 * It uses the provided [inputData] for the tensor at [targetIndex] or with [tensorName]
 * and defaults to zeros for others.
 */
class ImageInputProvider(
    private val inputData: Any,
    private val targetIndex: Int = 0,
    private val tensorName: String? = null
) : InputProvider {
    override fun createInput(tensor: Tensor, index: Int): Any {
        val useThisInput = if (tensorName != null) {
            tensor.name == tensorName
        } else {
            index == targetIndex
        }

        return if (useThisInput) {
            inputData
        } else {
            ZeroInputProvider.createInput(tensor, index)
        }
    }

    override fun createOutput(tensor: Tensor, index: Int): Any {
        return ZeroInputProvider.createOutput(tensor, index)
    }
}

/**
 * [InputProvider] that creates random-filled arrays. 
 * Often more effective than zeros for "waking up" all execution paths in some models.
 */
class RandomInputProvider(private val seed: Int = 42) : InputProvider {
    private val random = Random(seed)

    override fun createInput(tensor: Tensor, index: Int): Any = createRandomData(tensor.dataType, tensor.shape)
    override fun createOutput(tensor: Tensor, index: Int): Any = ZeroInputProvider.createOutput(tensor, index)

    private fun createRandomData(dataType: TensorDataType, shape: IntArray): Any {
        val totalElements = shape.fold(1) { acc, i -> acc * (if (i <= 0) 1 else i) }
        return when (dataType) {
            TensorDataType.FLOAT32 -> FloatArray(totalElements) { random.nextFloat() }
            TensorDataType.INT32 -> IntArray(totalElements) { random.nextInt() }
            TensorDataType.UINT8 -> ByteArray(totalElements) { random.nextInt(256).toByte() }
            TensorDataType.INT64 -> LongArray(totalElements) { random.nextLong() }
        }
    }
}


