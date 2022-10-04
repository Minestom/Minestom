plugins {
    id("java-library")
}

group = "net.minestom.testing"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Minestom API
    api(project(mapOf("path" to ":")))
    // Junit Testing Framework
    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.junit.suite.api)
    runtimeOnly(libs.junit.engine)
    runtimeOnly(libs.junit.suite.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}