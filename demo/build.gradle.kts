plugins {
    application
    id("minestom.common-conventions")
}

application {
    mainClass.set("net.minestom.server.demo.Main")
}

dependencies {
    implementation(rootProject)
    implementation(libs.jNoise)
}
