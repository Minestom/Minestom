plugins {
    `java-platform`
    `maven-publish`
    signing
}

group = "net.onelitefeather.microtus"

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
    maven(url = "https://jitpack.io")
}
dependencies {
    constraints {
        api(project(":testing"))
        api(rootProject)

        // Logging
        api(libs.bundles.logging)
        // Libraries required for the terminal
        api(libs.bundles.terminal)

        // Performance improving libraries
        api(libs.caffeine)
        api(libs.fastutil)
        api(libs.bundles.flare)

        // Libraries
        api(libs.gson)
        api(libs.jcTools)

        // Adventure, for user-interface
        api(libs.bundles.adventure)

        // Kotlin Libraries
        api(libs.bundles.kotlin)

        api(libs.maven.resolver)
        api(libs.maven.connector)
        api(libs.maven.transport.http)

        // Minestom Data (From MinestomDataGenerator)
        api(libs.minestomData)

        // BStats
        api(libs.bstats.base)
    }
}
javaPlatform {
    allowDependencies()
}

signing {
    if (!project.hasProperty("skip.signing") && !version.toString().endsWith("-SNAPSHOT")) {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        signing.isRequired
        sign(publishing.publications)
    }
}
publishing {
    publications {
        repositories {
            maven {
                name = "sonatype"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else {
                    "https://s01.oss.sonatype.org/service/local/"
                })

                credentials {
                    username = project.findProperty("sonatypeUsername") as String? ?: ""
                    password = project.findProperty("sonatypePassword") as String? ?: ""
                }
            }
        }
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
            pom {
                name.set(project.name)
                description.set("Bill of materials for Microtus projects.")
                url.set("https://github.com/OneLiteFeatherNET/microtus")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
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
                scm {
                    url.set("https://github.com/OneLiteFeatherNET/microtus")
                    connection.set("scm:git:https://github.com/OneLiteFeatherNET/microtus.git")
                    developerConnection.set("scm:git:git@github.com:OneLiteFeatherNET/microtus.git")
                    tag.set("${project.version}")
                }

                issueManagement{
                    system.set("GitHub")
                    url.set("https://github.com/OneLiteFeatherNET/microtus/issues")
                }
            }
        }
    }
}