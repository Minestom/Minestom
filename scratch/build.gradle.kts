plugins {
    `java-library`

    `maven-publish`
    signing
}

group = "net.minestom"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    // TODO: only depend on the `lib` module
    implementation(rootProject)
    api(libs.fastutil)
    implementation(libs.jcTools)


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}