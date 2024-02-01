pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BreadBoard"
include(":sample")
include(":framework:annotations")
include(":framework:launcher")
include(":framework:processor")
include(":sample:bizs:bizhome")
include(":framework:base")
includeBuild("framework/plugin")
include(":sample:bizs:bizPureJava")
include(":samplepurejava")
