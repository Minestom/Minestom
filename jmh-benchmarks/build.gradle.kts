plugins {
    id("minestom.java-library")
    alias(libs.plugins.jmh.plugin)
}

dependencies {
    jmhImplementation(rootProject)
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annotationprocessor)
}
