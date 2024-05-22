plugins {
    id("java-library")
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    signing
}

group = "net.onelitefeather.microtus.testing"


repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Minestom API
    api(project(mapOf("path" to ":")))
    // Junit Testing Framework
    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.junit.suite.api)
    runtimeOnly(libs.junit.engine)
    runtimeOnly(libs.junit.suite.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

indra {
    javaVersions {
        target(21)
        testWith(21)
    }

    github("OneLiteFeatherNET", "Microtus") {
        ci(true)
        publishing(false)
    }
    mitLicense()
    signWithKeyFromPrefixedProperties("onelitefeather")
    configurePublications {
        pom {
            developers {
                developer {
                    id.set("themeinerlp")
                    name.set("Phillipp Glanz")
                    email.set("p.glanz@madfix.me")
                }
                developer {
                    id.set("theEvilReaper")
                    name.set("Steffen Wonning")
                    email.set("steffenwx@gmail.com")
                }
            }
        }
    }
}