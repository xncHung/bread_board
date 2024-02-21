package io.xnchung.breadboard.gradle.plugin

import io.xnchung.breadboard.gradle.plugin.apicompat.CollectDataStrategy
import org.gradle.api.tasks.Input
import org.gradle.process.CommandLineArgumentProvider

internal class BreadBoardCmdLineArgumentProvider(
    @get:Input
    val forKsp: Boolean,
    @get:Input
    val enable: Boolean,
    @get:Input
    val collectDataStrategy: CollectDataStrategy,
    @get:Input
    val isApp: Boolean
) : CommandLineArgumentProvider {
    private val prefix = if (forKsp) "" else "-A"
    override fun asArguments(): Iterable<String> {
        return mapOf(
            "io.xnchung.breadboard.codegen.enable" to enable.toString(),
            "io.xnchung.breadboard.codegen.strategy" to collectDataStrategy.toString(),
            "io.xnchung.breadboard.codegen.isApp" to isApp.toString()
        ).map { (key, value) -> "$prefix${key}=$value" }
    }
}