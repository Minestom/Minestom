import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("minestom.java-binary")
}

dependencies {
    implementation(rootProject)

    runtimeOnly(libs.bundles.logback)
}

application {
    mainClass.set("net.minestom.demo.Main")
    applicationDefaultJvmArgs += "-ea"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("minestom-demo.jar")
}
