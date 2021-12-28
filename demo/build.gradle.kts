plugins {
    application
    id("minestom.common-conventions")
    id("minestom.native-conventions")
    id("com.github.johnrengelman.shadow") version("7.1.1")
}

application {
    // This might be a little redundant, but better safe than sorry.
    mainClass.set("net.minestom.demo.Main")
    @Suppress("DEPRECATION")
    mainClassName = "net.minestom.demo.Main"
}

dependencies {
    implementation(rootProject)
    implementation(libs.jNoise)
}