pluginManagement {
    repositories {
        maven {
            name = "localBuild"
            url = uri("framework/plugin/main/build/repo")
        }
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
//includeBuild("framework/plugin")
include(":sample")
include(":framework:annotations")
include(":framework:launcher")
include(":framework:processor")
include(":sample:bizs:bizhome")
include(":framework:base")
include(":sample:bizs:bizPureJava")
include(":samplepurejava")
