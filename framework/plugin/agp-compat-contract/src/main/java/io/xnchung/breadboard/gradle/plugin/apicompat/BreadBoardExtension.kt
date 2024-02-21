package io.xnchung.breadboard.gradle.plugin.apicompat

import org.gradle.api.Project

/**
 * configuration for BreadBoard plugin
 */
interface BreadBoardExtension {
    var excludeExternalLib: Boolean
}

enum class CollectDataStrategy {
    Hilt,
    Instrumentation
}

val Project.collectDataStrategy
    get() = findProperty("io.xnchung.breadboard.collectDataStrategy")?.toString()?.let { CollectDataStrategy.valueOf(it) } ?: run {
        if (plugins.hasPlugin("com.google.dagger.hilt.android")) {
            CollectDataStrategy.Hilt
        } else {
            CollectDataStrategy.Instrumentation
        }
    }