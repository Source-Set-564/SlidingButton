pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal() //For development purpose
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal() //For development purpose
    }
}
rootProject.name = "SS564 Sliding Button Library"
include(":app")
include(":slidingbutton")
