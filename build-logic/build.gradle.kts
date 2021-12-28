plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val indraVersion = "2.0.6"
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.6.10")
    implementation("net.kyori", "indra-common", indraVersion)
    implementation("net.kyori", "indra-publishing-sonatype", indraVersion)
    implementation("org.graalvm.buildtools", "native-gradle-plugin", "0.9.9")
}