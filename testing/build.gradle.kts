plugins {
    id("minestom.java-library")
    id("minestom.publishing")
}

dependencies {
    api(rootProject)

    implementation(libs.junit.api)
    implementation(libs.junit.params)
    implementation(libs.junit.suite.api)
    runtimeOnly(libs.junit.engine)
    runtimeOnly(libs.junit.suite.engine)
}
