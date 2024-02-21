package io.xnchung.breadboard.gradle.plugin

import com.android.build.api.AndroidPluginVersion
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.AndroidBasePlugin
import com.android.build.gradle.api.BaseVariant
import io.xnchung.breadboard.gradle.plugin.apicompat.BreadBoardExtension
import io.xnchung.breadboard.gradle.plugin.apicompat.CollectDataStrategy
import io.xnchung.breadboard.gradle.plugin.apicompat.collectDataStrategy
import io.xnchung.breadboard.gradle.plugin.apicompat.getAndroidComponentsExtension
import io.xnchung.breadboard.gradle.plugin.task.addJavaTaskProcessorOptions
import io.xnchung.breadboard.gradle.plugin.task.addKaptTaskProcessorOptions
import io.xnchung.breadboard.gradle.plugin.task.addKspTaskProcessorOptions
import io.xnchung.breadboard.gradle.plugin.task.isKspTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.CommandLineArgumentProvider

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
        val currentAGPVersion = currentAGPVersion()
        if (currentAGPVersion < AndroidPluginVersion(7, 0)) {
            error("The Breadboard plugin is only compatible with Android Gradle Plugin(AGP) version 7.0 or higher ,but found $currentAGPVersion")
        }
        val breadBoardExtension =
            extensions.create(BreadBoardExtension::class.java, "breadboard", BreadBoardExtensionImpl::class.java)
        configureBytecodeTransformASM(breadBoardExtension)
        configureProcessorFlags()
    }

    private fun Project.configureBytecodeTransformASM(breadBoardExtension: BreadBoardExtension) {
        if (plugins.hasPlugin(AppPlugin::class.java)) {
            getAndroidComponentsExtension().collectAndInjectConverterProvidersAndComponents(
                this@configureBytecodeTransformASM,
                breadBoardExtension
            )
        }
    }

    private fun Project.configureProcessorFlags() {
        val androidBaseExtension = baseExtension()
        val isApp = plugins.hasPlugin(AppPlugin::class.java)
        fun kotlinAtLeast170() =
            getKotlinPluginVersion(this)?.let { parseKotlinVersion(it) }?.let { it >= KotlinVersion(1, 7) }
        androidBaseExtension.forEachRootVariant {
            if (kotlinAtLeast170() == false) {
                val debuggable = it.buildType.isDebuggable
                it.javaCompileOptions.annotationProcessorOptions.compilerArgumentProviders.add(
                    BreadBoardCmdLineArgumentProvider(false, debuggable, collectDataStrategy, isApp)
                )
            }
        }
        getAndroidComponentsExtension().onAllVariants {
            buildType?.let { buildType -> androidBaseExtension.buildTypes.findByName(buildType) }?.also {
                val debuggable = it.isDebuggable
                val produceArgProvider: (Task) -> CommandLineArgumentProvider = {
                    BreadBoardCmdLineArgumentProvider(it.isKspTask(), debuggable, collectDataStrategy, isApp)
                }
                addKspTaskProcessorOptions(this, produceArgProvider)
                if (kotlinAtLeast170() != false) {
                    addJavaTaskProcessorOptions(this, produceArgProvider)
                    addKaptTaskProcessorOptions(this, produceArgProvider)
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