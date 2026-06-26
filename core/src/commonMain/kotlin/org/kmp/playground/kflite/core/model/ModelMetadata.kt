package org.kmp.playground.kflite.core.model

data class ModelMetadata(
    val name: String?,
    val description: String?,
    val author: String?,
    val version: String?,
    val license: String?,
    val labels: List<String>?,
    val inputs: List<TensorMetadata>,
    val outputs: List<TensorMetadata>
)

data class TensorMetadata(
    val name: String,
    val shape: IntArray,
    val dataType: TensorDataType
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TensorMetadata
        if (name != other.name) return false
        if (!shape.contentEquals(other.shape)) return false
        if (dataType != other.dataType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + shape.contentHashCode()
        result = 31 * result + dataType.hashCode()
        return result
    }
}
