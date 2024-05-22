import java.time.Duration
import java.util.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.2.1"
    id("org.cadixdev.licenser") version "0.6.1"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-1"
    signing
}

var baseVersion by extra("1.0.1")
var snapshot by extra("-SNAPSHOT")

group = "net.onelitefeather.microtus"


version = "%s%s".format(Locale.ROOT, baseVersion, snapshot)

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}
gradlePlugin {
    // website.set("https://github.com/OneLiteFeatherNET/Microtus")
    // vcsUrl.set("https://github.com/OneLiteFeatherNET/Microtus")
    plugins {
        register("extension") {
            id = "net.onelitefeather.microtus.extension"
            displayName = "Extension (Minestom)"
            description = "Generate extension.json for Minestom extensions based on the Gradle project"
            implementationClass = "net.onelitefeather.microtus.ExtensionPlugin"
            // tags.set(listOf("minestom", "microtus", "extension"))
        }
    }
}

nexusPublishing{
    useStaging.set(true)

    transitionCheckOptions {
        maxRetries.set(360) // 1 hour
        delayBetween.set(Duration.ofSeconds(10))
    }

    repositories.sonatype {
        nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
        snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

        if (System.getenv("SONATYPE_USERNAME") != null) {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/OneLiteFeatherNET/Microtus")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
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