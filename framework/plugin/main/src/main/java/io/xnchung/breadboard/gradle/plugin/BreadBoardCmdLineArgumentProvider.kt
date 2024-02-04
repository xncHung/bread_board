package io.xnchung.breadboard.gradle.plugin

import org.gradle.api.tasks.Input
import org.gradle.process.CommandLineArgumentProvider

internal class BreadBoardCmdLineArgumentProvider(
    @get:Input
    val forKsp: Boolean,
    @get:Input
    val enable: Boolean
) : CommandLineArgumentProvider {
    private val prefix = if (forKsp) "" else "-A"
    override fun asArguments(): Iterable<String> {
        return mapOf(
            "io.xnchung.breadboard.codegen.enable" to enable.toString()
        ).map { (key, value) -> "$prefix${key}=$value" }
    }
}