@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}


dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(gradleApi())
}