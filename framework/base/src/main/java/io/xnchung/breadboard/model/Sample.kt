package io.xnchung.breadboard.model

sealed interface SampleContract {
    val id: String
    val name: String
    val description: String
    val tags: List<String>
    val documentation: String
    val owners: List<String>
    val target: Target
    val route: String
    val source: String
    val paramHolders: List<ParamHolderContract<*>>
    val moduleName: String
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
    override val route: String,
    override val source: String,
    override val paramHolders: List<ParamHolderContract<*>>,
    override val moduleName: String
) : SampleContract
