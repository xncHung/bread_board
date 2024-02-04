package io.xnchung.breadboard.gradle.plugin.apicompat.impl

import com.android.build.api.artifact.Artifacts
import com.android.build.api.variant.Component
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat

class ComponentCompatApi71Impl(private val component: Component) : ComponentCompat() {

    override val name: String = component.name
    override val buildType: String? = component.buildType
    override val artifacts: Artifacts = component.artifacts
}
