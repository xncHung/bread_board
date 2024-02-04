package io.xnchung.breadboard.gradle.plugin.apicompat

import com.android.build.api.AndroidPluginVersion
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.AndroidComponentsExtensionCompatApi70Impl
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.AndroidComponentsExtensionCompatApi71Impl
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.AndroidComponentsExtensionCompatApi72Impl
import io.xnchung.breadboard.gradle.plugin.apicompat.impl.AndroidComponentsExtensionCompatApi80Impl
import io.xnchung.breadboard.gradle.plugin.currentAGPVersion
import org.gradle.api.Project

fun Project.getAndroidComponentsExtension(): AndroidComponentsExtensionCompat {
    val version = currentAGPVersion()
    return when {
        version >= AndroidPluginVersion(8, 0) -> {
            AndroidComponentsExtensionCompatApi80Impl(this)
        }
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