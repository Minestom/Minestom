import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id ("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(rootProject)
    implementation(libs.bundles.adventure)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.bytemc.minestom.bootstrap.MinestomBootstrap"
    }

    dependsOn(":" + rootProject.path + ":shadowJar")
}

tasks.withType<ShadowJar> {
    archiveFileName = "bootstrap-${project.version}.jar"
}