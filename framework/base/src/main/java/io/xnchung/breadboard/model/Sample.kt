package io.xnchung.breadboard.model

sealed interface SampleContract {
    val id: String
    val name: String
    val description: String
    val tags: List<String>
    val documentation: String
    val owners: List<String>
    val target: Target
    val routes: List<String>
    val source: String
    val paramHolders: List<ParamHolderContract<*>>
}

@Suppress("unused")
data class Sample(
    override val id: String,
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
