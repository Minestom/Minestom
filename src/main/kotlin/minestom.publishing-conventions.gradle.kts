plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    id("net.kyori.indra.publishing.sonatype")
    id("io.github.gradle-nexus.publish-plugin")
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

indraSonatype {
    useAlternateSonatypeOSSHost("s01")
}