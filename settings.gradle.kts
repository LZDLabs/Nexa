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
        google()
        mavenCentral()
    }
}

rootProject.name = "Nexa"

include(":protocol") // Pure Kotlin: wire protocol + pairing (shared by both apps)
include(":ui")       // Android library: NEXA design system (Gemini UI tokens/components)
include(":mobile")   // NEXA Mobile (phone)
include(":tv")       // NEXA TV (TCL Android TV)
