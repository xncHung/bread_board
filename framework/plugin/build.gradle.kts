@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
gradlePlugin {
    plugins {
        create("breadboard") {
            id = "io.xncHung.breadboard.plugin"
            version = "1.0-SNAPSHOT"
            implementationClass = "io.xnchung.breadboard.gradle.plugin.BreadBoardPlugin"
        }
    }
}
group = "io.xncHung.breadboard.plugin"
dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.google.ksp.plugin)
    implementation(libs.asm)
    implementation(gradleApi())
    implementation(libs.javapoet)

    testImplementation(gradleTestKit())
    testImplementation(libs.junit)

}