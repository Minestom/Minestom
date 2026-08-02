plugins {
    java
    application
    id("minestom.style")
}

//TODO(valhalla) revert to 25 when the branch no longer requires the valhalla jdk
val javaVersion = System.getenv("JAVA_VERSION") ?: "28"

group = "net.minestom"

repositories {
    val dataVersion = libs.minestomData.get().version ?: ""
    if (dataVersion.endsWith("-dev"))
        mavenLocal()
    val adventureVersion = libs.adventure.api.get().version ?: ""
    if (adventureVersion.endsWith("-SNAPSHOT"))
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/")

    mavenCentral()
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    modularity.inferModulePath = true
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    //TODO(valhalla) revert when gradle supports jdk28 (module-info.class main class stamping cannot parse class file 72)
    options.javaModuleMainClass.unsetConvention()
}

tasks.withType<JavaExec> {
    //TODO(valhalla) revert when jep 401 leaves preview
    jvmArgs("--enable-preview")
}
