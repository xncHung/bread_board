package io.xnchung.breadboard.gradle.plugin.apicompat

import com.android.build.api.AndroidPluginVersion
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.agp70.AndroidComponentsExtensionCompatApi70Impl
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.agp71.AndroidComponentsExtensionCompatApi71Impl
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.agp72.AndroidComponentsExtensionCompatApi72Impl
import io.xnchung.breadboard.gradle.plugin.currentAGPVersion
import org.gradle.api.Project

fun Project.getAndroidComponentsExtension(): AndroidComponentsExtensionCompat {
    val version = currentAGPVersion
    return when {
        version >= AndroidPluginVersion(7, 2) -> {
            AndroidComponentsExtensionCompatApi72Impl(this)
        }

        version >= AndroidPluginVersion(7, 1) -> {
            AndroidComponentsExtensionCompatApi71Impl(this)
        }

        version >= AndroidPluginVersion(7, 0) -> {
            AndroidComponentsExtensionCompatApi70Impl(this)
        }

        else -> {
            error("Android Gradle Plugin $version is not supported")
        }
    }
}