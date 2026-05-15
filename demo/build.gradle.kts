import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("minestom.java-binary")
}

dependencies {
    implementation(rootProject)

    runtimeOnly(libs.bundles.logback)
}

application {
    mainClass.set("net.minestom.demo.server.AllInOneServer")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("minestom-demo.jar")
}

// One `runX` task per launcher in src/main/java/net/minestom/demo/server.
// Example: `./gradlew :demo:runChat`, `./gradlew :demo:runBlocks`.
val serversDir = file("src/main/java/net/minestom/demo/server")
val serverFiles = serversDir.listFiles { f ->
    f.isFile && f.name.endsWith("Server.java")
} ?: emptyArray()

serverFiles.forEach { file ->
    val className = file.nameWithoutExtension // e.g. ChatServer
    val taskName = "run" + className.removeSuffix("Server")
        .ifEmpty { "Server" } // shouldn't happen, but safe
    tasks.register<JavaExec>(taskName) {
        group = "demo"
        description = "Run net.minestom.demo.server.$className"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("net.minestom.demo.server.$className")
        standardInput = System.`in`
    }
}
