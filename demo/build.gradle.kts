plugins {
    java
    application
}

application {
    mainClass.set("net.minestom.server.demo.Main")
}

dependencies {
    implementation(rootProject)
    implementation(libs.jNoise)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
