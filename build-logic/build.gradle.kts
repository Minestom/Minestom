plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val indraVersion = "3.1.3"
    implementation("org.jetbrains.kotlin", "kotlin-gradle-plugin", "2.0.0")
    implementation("net.kyori", "indra-common", indraVersion)
    implementation("net.kyori", "indra-common", indraVersion)
    implementation("net.kyori", "indra-publishing-sonatype", indraVersion)
    implementation("org.graalvm.buildtools", "native-gradle-plugin", "0.9.28")
    implementation("io.github.gradle-nexus", "publish-plugin", "2.0.0-rc-1")
}
