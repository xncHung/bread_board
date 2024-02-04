@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":agp-compat-contract"))
    implementation(project(":agp-compat-7-0"))
    implementation(project(":agp-compat-7-1"))
    implementation(project(":agp-compat-7-2"))
    implementation(project(":agp-compat-8-0"))
    compileOnly(libs.android.gradle.plugin)
    compileOnly(gradleApi())
}