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
        // This line tells Gradle where to find the calendar library
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "FeeTracker"
include(":app")