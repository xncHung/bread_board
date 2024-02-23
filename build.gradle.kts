import io.xnchung.breadboard.groupName
import io.xnchung.breadboard.isDevMode

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    id("org.jetbrains.kotlin.android") version (libs.versions.kotlin) apply false
    alias(libs.plugins.google.ksp.plugin) apply false
    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.breadboard) apply false
    alias(libs.plugins.hilt) apply false
}
subprojects {
    if (isDevMode()) {
        afterEvaluate {
            configurations.all {
                resolutionStrategy.dependencySubstitution.all {
                    requested.also {
                        if ((it as? ModuleComponentSelector)?.group == groupName) {
                            findProject(":framework:${it.module}")?.also {
                                useTarget(it)
                            }
                        }
                    }
                }
            }
        }
    }
    if (path.startsWith(":framework")) {
        group = groupName
        val mavenPublish = "maven-publish"
        apply(plugin = mavenPublish)
        plugins.withId(mavenPublish) {
            extensions.getByType(PublishingExtension::class.java).apply {
                repositories {
                    maven {
                        name = "localMockRepo"
                        url = uri(File(rootDir, "framework/localRepo"))
                    }
                }
            }
        }
    }
}
