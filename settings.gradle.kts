enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
		maven {
			name = "tecno-repo"
			url = uri("https://repo.mrtecno.tk/repository/public-maven/")
		}
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

rootProject.name = "Minestom-T"
include("code-generators")
include("jmh-benchmarks")
include("jcstress-tests")
include("demo")
include("testing")
