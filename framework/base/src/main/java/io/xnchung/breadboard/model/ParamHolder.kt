package io.xnchung.breadboard.model

interface TypeToken<T>
sealed interface ParamHolderContract<T> : TypeToken<T> {
    val key: String
    val defaultValue: String
    val qualifier: String
}

data class ParamHolder<T>(
    override val key: String,
    override val defaultValue: String,
    override val qualifier: String
) : ParamHolderContract<T>

