package io.xnchung.breadboard.gradle.plugin

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.api.BaseVariant
import io.xnchung.breadboard.gradle.plugin.task.addKspTaskProcessorOptions
import org.gradle.api.Plugin
import org.gradle.api.Project

class BreadBoardPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        var configured = false
        with(target) {
            plugins.withType(AndroidBasePlugin::class.java) {
                configured = true
                configureBreadBoard()
            }
            afterEvaluate {
                check(configured) {
                    "The Breadboard plugin can only applied to an Android project."
                }
                it.verifyDependencies()
            }
        }
    }

    private fun Project.verifyDependencies() {

    }

    private fun Project.configureBreadBoard() {
        val currentAGPVersion = currentAGPVersion
        if (currentAGPVersion < AndroidPluginVersion(7, 0)) {
            error("The Breadboard plugin is only compatible with Android Gradle Plugin(AGP) version 7.0 or higher ,but found $currentAGPVersion")
        }
        configureBytecodeTransformASM()
        configureProcessorFlags()
    }

    private fun Project.configureBytecodeTransformASM() {

    }

    private fun Project.configureProcessorFlags() {
        val androidExtension = requireNotNull(baseExtension()) { "Android BaseExtension not found." }
        extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant: Variant ->
            variant.buildType?.let { buildType -> androidExtension.buildTypes.findByName(buildType) }?.also {
                val debuggable = it.isDebuggable
                addKspTaskProcessorOptions(variant) {
                    BreadBoardCmdLineArgumentProvider(true, debuggable)
                }
            }
        }
    }

//    private fun configureCompileClasspath(project: Project, breadBoardExtension: BreadBoardExtension) {
//        val androidExtension = requireNotNull(project.baseExtension()) { "Android BaseExtension not found." }
//        androidExtension.forEachRootVariant { variant ->
//            configureVariantCompileClasspath(project, breadBoardExtension, androidExtension, variant)
//        }
//    }


    private fun BaseExtension.forEachRootVariant(
        @Suppress("DEPRECATION") block: (BaseVariant) -> Unit
    ) {
        when (this) {
            is AppExtension -> {
                applicationVariants.all { block(it) }
            }

            is LibraryExtension -> {
                libraryVariants.all { block(it) }
            }

            is TestExtension -> {
                applicationVariants.all { block(it) }
            }

            else -> error("breadboard plugin does not know how to configure '$this'")
        }
    }
}