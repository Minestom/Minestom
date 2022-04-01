import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("me.champeau.jmh") version ("0.6.6")
    id("minestom.common-conventions")
    kotlin("jvm") version "1.6.20-RC2"
}

dependencies {
    jmhImplementation(rootProject)
    jmh(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annotationprocessor)
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}