import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(rootProject)
    // https://mvnrepository.com/artifact/org.jctools/jctools-core
    implementation("org.jctools:jctools-core:4.0.3")

    runtimeOnly(libs.bundles.logback)
}

tasks {
    application {
        mainClass.set("net.minestom.demo.Main")
    }

    withType<ShadowJar> {
        archiveFileName.set("minestom-demo.jar")
    }
}