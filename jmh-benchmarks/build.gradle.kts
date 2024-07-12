plugins {
    id("me.champeau.jmh") version ("0.7.2")
}

dependencies {
    jmhImplementation(rootProject)
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annotationprocessor)
}
