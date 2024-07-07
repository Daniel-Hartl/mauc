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
    versionCatalogs {
        create("libs") {
            // Stellen Sie sicher, dass die libs.versions.toml Datei existiert und korrekt ist
            from(files("gradle/libs.versions.toml"))
        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PulsMesser"
include(":app")
 