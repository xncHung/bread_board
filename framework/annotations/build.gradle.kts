import io.xnchung.breadboard.declareMavenPublish

plugins {
    id("kotlin")
}
version = libs.versions.breadboard.annotations.get()
publishing {
    this.declareMavenPublish(project)
}