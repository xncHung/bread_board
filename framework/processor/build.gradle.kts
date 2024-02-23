import io.xnchung.breadboard.declareMavenPublish

plugins {
    id("kotlin")
}
version = libs.versions.breadboard.processor.get()
publishing {
    declareMavenPublish(project)
}
dependencies {
    implementation(libs.breadboard.annotations)
    implementation(libs.androidx.annotation)
    implementation(libs.google.ksp.api)
    implementation(libs.javapoet)
}