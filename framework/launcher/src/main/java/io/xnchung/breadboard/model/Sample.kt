package io.xnchung.breadboard.model

data class Sample(
    val name: String,
    val description: String,
    val tags: List<String>,
    val documentation: String,
    val owners: List<String>,
    val target: Target,
    val routes: List<String>,
    val paramHolders: List<ParamHolder<*>>
)
