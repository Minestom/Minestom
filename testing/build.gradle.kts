plugins {
    `java-library`
}

group = "net.minestom.testing"

dependencies {
    api(rootProject)

    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.junit.suite.api)
    runtimeOnly(libs.junit.engine)
    runtimeOnly(libs.junit.suite.engine)
}
