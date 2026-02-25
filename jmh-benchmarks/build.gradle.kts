plugins {
    id("minestom.java-library")
    alias(libs.plugins.jmh.plugin)
}

dependencies {
    implementation(libs.fastutil)
    jmhImplementation(rootProject)
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annotationprocessor)
}
