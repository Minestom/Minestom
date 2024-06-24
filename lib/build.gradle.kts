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
    api(libs.jetbrainsAnnotations)
    api(libs.bundles.adventure)

    // Performance/data structures
    implementation(libs.caffeine)

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
