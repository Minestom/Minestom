import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("minestom.common-conventions")
    id("minestom.native-conventions")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
//    id("net.onelitefeather.microtus.extension")
}

application {
    mainClass.set("net.minestom.demo.Main")
    // This is included because Shadow is buggy. Wait for https://github.com/johnrengelman/shadow/issues/613 to befixed.
}

dependencies {
//    extensionLibrary("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation(rootProject)
    implementation(libs.jNoise)
}

tasks.withType<ShadowJar> {
    archiveFileName.set("minestom-demo.jar")
}

/*extension {
    authors = listOf("yolo")
    entrypoint = "net.onelitefeather.microtus.extension.Test"
}*/