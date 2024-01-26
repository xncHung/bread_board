// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("com.android.application") version "7.2.0" apply false
    id("org.jetbrains.kotlin.android") version (libs.versions.kotlin) apply false
    alias(libs.plugins.google.ksp.plugin) apply false
    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.com.android.library) apply false
    alias(libs.plugins.breadboard) apply false
}