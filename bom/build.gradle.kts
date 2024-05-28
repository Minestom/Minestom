plugins {
    id("io.github.gradlebom.generator-plugin") version "1.0.0.Final"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    signing
}

group = "net.onelitefeather.microtus"

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
    maven(url = "https://jitpack.io")
}

bomGenerator {
    val version = rootProject.version as String
    includeDependency("net.onelitefeather.microtus", "Microtus", version)
    includeDependency("net.onelitefeather.microtus.testing", "testing", version)
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
        create<MavenPublication>("maven") {

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
