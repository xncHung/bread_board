package io.xnchung.breadboard.model

import io.xnchung.breadboard.annotations.ParamProviderContract

interface TypeToken<out T>
interface ParamHolderContract<out T> {
    val key: String
    val defaultValue: String
    val qualifier: String
    val desc: String
    val nullable: Boolean
    val provider: ParamProviderContract<T>?
    val typeToken: TypeToken<T>
}

data class ParamHolder<out T>(
    override val key: String,
    override val desc: String,
    override val defaultValue: String,
    override val qualifier: String,
    override val nullable: Boolean,
    override val provider: ParamProviderContract<T>?,
    override val typeToken: TypeToken<T>
) : ParamHolderContract<T>

