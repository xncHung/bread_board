package io.xnchung.breadboard.gradle.plugin.instrumentation

internal data class AsmMethodInfo(
    val owner: String,
    val name: String,
    val descriptor: String
)