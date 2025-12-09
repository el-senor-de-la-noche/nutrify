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
    repositories {
        google()
        mavenCentral()

        // Repositorio oficial de Gemini
        maven { url = uri("https://dl.google.com/public/gemini/maven") }
    }
}



rootProject.name = "Nutrify"
include(":app")
 