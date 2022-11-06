plugins {
    id("java-library")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.minestom.testing"
            artifactId = "testing"
            version = "1.0"

            from(components["java"])
        }
    }
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