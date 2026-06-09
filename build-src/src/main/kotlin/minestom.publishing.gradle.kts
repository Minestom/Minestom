plugins {
    `maven-publish`
    signing
    alias(libs.plugins.nmcp)
}

val minestomDataVersion = libs.minestomData.get().version
        ?: throw IllegalStateException("unable to determine Minecraft version")
val mcVersion = minestomDataVersion.split("-")[0]

publishing.publications.create<MavenPublication>("maven") {
    groupId = project.group.toString()
    artifactId = project.name // eg "minestom" or "testing"
    version = project.version.toString()

    from(project.components["java"])

    pom {
        name.set(this@create.artifactId)
        description.set("$mcVersion Lightweight Minecraft server")
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

tasks.register<CheckAbiTask>("checkBinaryCompatibility") {
    group = "verification"
    description = "Checks binary compatibility against the baseline released version."

    onlyIf { // Requires to be running in CI, as we check out the target branch for ABI breaks.
        System.getenv("CI") == "true"
    }

    oldJar = layout.file(provider {
        project.findProperty("baselineJarDir")?.toString()
                ?.let { dir -> project.rootProject.file("$dir/${project.name}.jar") }
                ?.takeIf { it.exists() }
    })

    newJar = tasks.named<Jar>("jar").flatMap { it.archiveFile }
}

