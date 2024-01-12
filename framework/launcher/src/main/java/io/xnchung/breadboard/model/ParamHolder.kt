package io.xnchung.breadboard.model

data class ParamHolder<T>(
    val key: String,
    val defaultValue: String,
    val converter: String.() -> T,
)
