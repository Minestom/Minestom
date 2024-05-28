plugins {
    id("io.github.gradlebom.generator-plugin") version "1.0.0.Final"
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    signing
}

group = "net.onelitefeather.microtus"

bomGenerator {
    val version = rootProject.version as String
    includeDependency("net.onelitefeather.microtus", "Microtus", version)
    includeDependency("net.onelitefeather.microtus.testing", "testing", version)
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
    description = "Bill of materials for Microtus projects."
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