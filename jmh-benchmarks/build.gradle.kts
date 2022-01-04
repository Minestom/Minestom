plugins {
    id("me.champeau.jmh") version ("0.6.6")
    id("minestom.common-conventions")
}

dependencies {
    jmhImplementation(rootProject)
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annotationprocessor)
}