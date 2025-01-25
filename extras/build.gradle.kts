plugins {
    `java-library`
    `maven-publish`
    signing
}

val channel = System.getenv("MINESTOM_CHANNEL") ?: "local" // local, snapshot, release
group = "net.minestom.extra"
val shortDescription = "1.21 Lightweight Minecraft server extras"

repositories {
    mavenCentral()
}

dependencies {
    api(rootProject)
    // Testing
    testImplementation(libs.bundles.junit)
    testImplementation(project(":testing"))
}

tasks {
    test {
        useJUnitPlatform()
    }
    publishing.publications.create<MavenPublication>("maven") {
        groupId = "net.minestom"
        // todo: decide on publishing scheme
        artifactId = if (channel == "snapshot") "minestom-snapshots" else "minestom"
        version = project.version.toString()

        from(project.components["java"])

        pom {
            name.set(this@create.artifactId)
            description.set(shortDescription)
            url.set("https://github.com/minestom/minestom")

            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://github.com/minestom/minestom/blob/main/LICENSE")
                }
            }

            developers {
                developer {
                    id.set("TheMode")
                }
                developer {
                    id.set("mworzala")
                    name.set("Matt Worzala")
                    email.set("matt@hollowcube.dev")
                }
            }

            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/minestom/minestom/issues")
            }

            scm {
                connection.set("scm:git:git://github.com/minestom/minestom.git")
                developerConnection.set("scm:git:git@github.com:minestom/minestom.git")
                url.set("https://github.com/minestom/minestom")
                tag.set("HEAD")
            }

            ciManagement {
                system.set("Github Actions")
                url.set("https://github.com/minestom/minestom/actions")
            }
        }
    }

    signing {
        isRequired = System.getenv("CI") != null

        val privateKey = System.getenv("GPG_PRIVATE_KEY")
        val keyPassphrase = System.getenv()["GPG_PASSPHRASE"]
        useInMemoryPgpKeys(privateKey, keyPassphrase)

        sign(publishing.publications)
    }
}