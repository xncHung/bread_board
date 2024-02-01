plugins {
    id("kotlin")
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}
dependencies {
    implementation(project(":framework:annotations"))
    implementation(libs.androidx.annotation)
    implementation(libs.google.ksp.api)
    implementation(libs.javapoet)
}