pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://jitpack.io")}
        maven { url = uri("https://repo.eclipse.org/content/repositories/paho-releases/")}
        google()
        mavenCentral()
    }
}
rootProject.name = "Android_Puls"
include(":app")

 