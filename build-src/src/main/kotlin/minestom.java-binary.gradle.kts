plugins {
    java
    application
    id("com.gradleup.shadow")
}

val javaVersion = System.getenv("JAVA_VERSION") ?: "21"

group = "net.minestom"

repositories {
    val dataVersion = libs.minestomData.get().version ?: ""
    if (dataVersion.endsWith("-dev"))
        mavenLocal()
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
