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
rootProject.name = "BreadBoard-gradle-plugin"
include(":main")
include(":agp-compat-impl")
include(":agp-compat-contract")
include(":agp-compat-7-0")
include(":agp-compat-7-1")
include(":agp-compat-7-2")
include(":agp-compat-8-0")
include(":transform-7x")
include("transform-asm")
