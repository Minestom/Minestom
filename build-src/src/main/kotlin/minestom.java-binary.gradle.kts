plugins {
    java
    application
    id("com.gradleup.shadow")
}

val javaVersion = System.getenv("JAVA_VERSION") ?: "25"

group = "net.minestom"

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    modularity.inferModulePath = true
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    mergeServiceFiles()
}
