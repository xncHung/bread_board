package io.xnchung.breadboard.model

sealed interface SampleContract {
    val name: String
    val description: String
    val tags: List<String>
    val documentation: String
    val owners: List<String>
    val target: Target
    val routes: List<String>
    val source: String
    val paramHolders: List<ParamHolder<*>>
}

@Suppress("unused")
data class Sample(
    override val name: String,
    override val description: String,
    override val tags: List<String>,
    override val documentation: String,
    override val owners: List<String>,
    override val target: Target,
    override val routes: List<String>,
    override val source: String,
    override val paramHolders: List<ParamHolder<*>>
) : SampleContract
