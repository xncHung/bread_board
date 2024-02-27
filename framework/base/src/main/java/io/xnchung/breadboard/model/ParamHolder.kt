package io.xnchung.breadboard.model

import io.xnchung.breadboard.annotations.ParamProviderContract

interface TypeToken<out T>
sealed interface ParamHolderContract<out T> : TypeToken<T> {
    val key: String
    val defaultValue: String
    val qualifier: String
    val desc: String
    val provider: ParamProviderContract<T>?
}

data class ParamHolder<out T>(
    override val key: String,
    override val desc: String,
    override val defaultValue: String,
    override val qualifier: String,
    override val provider: ParamProviderContract<T>?,
) : ParamHolderContract<T>

