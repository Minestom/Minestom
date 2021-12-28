plugins {
    application
    id("minestom.common-conventions")
    id("minestom.native-conventions")
}

application {
    mainClass.set("net.minestom.demo.Main")
}

dependencies {
    implementation(rootProject)
    implementation(libs.jNoise)
}
