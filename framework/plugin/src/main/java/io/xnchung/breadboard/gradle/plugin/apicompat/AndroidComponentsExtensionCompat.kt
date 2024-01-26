package io.xnchung.breadboard.gradle.plugin.apicompat

import io.xnchung.breadboard.gradle.plugin.BreadBoardExtension
import org.gradle.api.Project


interface AndroidComponentsExtensionCompat {

    fun onAllVariants(block: ComponentCompat.() -> Unit)
    fun collectAndInjectConverterProvidersAndComponents(project: Project, breadBoardExtension: BreadBoardExtension)
}
