package io.xnchung.breadboard.gradle.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project

fun Project.currentAGPVersion() = extensions.getByType(AndroidComponentsExtension::class.java).pluginVersion
fun Project.baseExtension(): BaseExtension = requireNotNull(extensions.findByType(BaseExtension::class.java)) { "Android BaseExtension not found." }
fun Project.androidComponentsExtension(): AndroidComponentsExtension<*, *, *> = extensions.getByType(AndroidComponentsExtension::class.java)

internal  fun getKotlinPluginVersion(project: Project): String? {
    val plugin = project.plugins.findPlugin("kotlin-android") ?: return null
    return try {
        // No null checks below because we're catching all exceptions.
        // KGP 1.7.0+ has getPluginVersion and older version have getKotlinPluginVersion
        val method = plugin.javaClass.methods.first {
            it.name == "getKotlinPluginVersion" || it.name == "getPluginVersion"
        }
        method.isAccessible = true
        method.invoke(plugin).toString()
    } catch (e: Throwable) {
        // Defensively catch all exceptions because we don't want it to crash
        // if kotlin plugin code changes unexpectedly.
        "unknown"
    }
}

fun parseKotlinVersion(currVersion: String): KotlinVersion? {
    return try {
        val parts = currVersion.split(".")
        val major = parts[0]
        val minor = parts[1]
        // We ignore the extensions, eg. "-RC".
        val patch = parts[2].substringBefore('-')
        return KotlinVersion(
            major.toInt(),
            minor.toInt(),
            patch.toInt()
        )
    } catch (e: Throwable) {
        null
    }
}