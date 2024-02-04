@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.jvm)
}


dependencies {
    api(project(":agp-compat-contract"))
    implementation(project(":transform-asm"))
    compileOnly(libs.android.gradle.plugin.get().run{ "${module.group}:${module.name}:8.0.0" })
    compileOnly(gradleApi())
}