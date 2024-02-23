@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.gradle.publish)
}
configurations.create("shadowed") {
    configurations.compileOnly.get().extendsFrom(this)

}
tasks.jar {
    archiveClassifier.set("before-jarjar")
}
tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    configurations = listOf(project.configurations["shadowed"])
    dependencies {
        exclude("org.jetbrains.kotlin::")
        exclude("org.jetbrains:annotations:")
    }
}

val pluginId = "io.xnchung.breadboard.plugin"
val pluginArtifactId = "breadboard-android-gradle-plugin"
publishing {
    repositories {
        maven {
            name = "localBuild"
            url = uri("$buildDir/repo")
        }
    }
}
gradlePlugin {
    website.set("https://github.com")
    vcsUrl.set("https://github.com")
    plugins {
        create("breadboard") {
            id = pluginId
            displayName = "Breadboard"
            description = "Gradle Plugin for Breadboard"
            tags.add("Breadboard")
            implementationClass = "io.xnchung.breadboard.gradle.plugin.BreadBoardPlugin"
        }
    }
}
group = "io.xnchung.breadboard"
version = "1.0"
dependencies {
    "shadowed"(project(":agp-compat-impl"))
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.google.ksp.plugin)
    implementation(libs.asm)
    implementation(gradleApi())
    implementation(libs.javapoet)

    testImplementation(gradleTestKit())
    testImplementation(libs.junit)

}