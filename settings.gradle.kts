enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
        maven("https://repo.spongepowered.org/maven")
        mavenCentral()
    }
    versionCatalogs {
        create("global") {
            from(files("gradle/global.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "Minestom"
include("code-generators")
include("demo")
