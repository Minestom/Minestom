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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api(libs.jetbrainsAnnotations)
}

tasks.test {
    useJUnitPlatform()
}
