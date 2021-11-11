plugins {
    java
    application
}

application {
    mainClass.set("net.minestom.server.demo.Main")
}

dependencies {
    implementation(rootProject)
    implementation("com.github.Articdive:JNoise:3.0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
