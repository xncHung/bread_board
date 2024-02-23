package io.xnchung.breadboard

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get

const val groupName = "io.xnchung.breadboard"
fun PublishingExtension.declareMavenPublish(
    project: Project,
    config: MavenPublication.() -> Unit = { from(project.components["kotlin"]) }
) {
    publications {
        create("maven", MavenPublication::class.java, config)
    }
}

fun Project.isDevMode(): Boolean = findProperty("devMode")?.toString()?.toBoolean() == true