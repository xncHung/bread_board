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
        maven {
            name = "localMockRepo"
            url = uri("framework/localRepo")
        }
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
include(":sample:bizs:bizPureJava")
include(":samplepurejava")