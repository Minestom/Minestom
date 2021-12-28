plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    val indraVersion = "2.0.6"
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.6.10")
    implementation("net.kyori", "indra-common", indraVersion)
    implementation("net.kyori", "indra-publishing-sonatype", indraVersion)
}