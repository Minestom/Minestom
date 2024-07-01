plugins {
    `java-library`
    id("java")
}

group = "net.minestom"
version = "dev"

repositories {
    mavenCentral()
}

dependencies {
    // Core dependencies
    implementation(libs.minestomData)
    api(libs.jetbrainsAnnotations)
    api(libs.bundles.adventure)

    // Performance/data structures
    implementation(libs.caffeine)
    api(libs.fastutil)
    api(libs.gson)

    // Testing
    testImplementation(libs.bundles.junit)
}

tasks.test {
    useJUnitPlatform()
}
