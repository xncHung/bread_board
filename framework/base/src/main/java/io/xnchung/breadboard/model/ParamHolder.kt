package io.xnchung.breadboard.model

data class ParamHolder<T>(
    val key: String,
    val defaultValue: String,
    val qualifier: String
) : ParamTypeToken<T>

interface ParamTypeToken<T>
