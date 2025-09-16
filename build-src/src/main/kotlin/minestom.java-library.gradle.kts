plugins {
    `java-library`

    `maven-publish`
    signing
    alias(libs.plugins.nmcp)
}

val javaVersion = System.getenv("JAVA_VERSION") ?: "25"

group = "net.minestom"
version = System.getenv("MINESTOM_VERSION") ?: "dev"

configurations.all {
    // We only use Jetbrains Annotations
    exclude("org.checkerframework", "checker-qual")
}

repositories {
    mavenCentral()
}

dependencies {
    // Core dependencies
    api(libs.jetbrainsAnnotations)

    // Testing
    testImplementation(libs.bundles.junit)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    modularity.inferModulePath = true

    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    (options as? StandardJavadocDocletOptions)?.apply {
        encoding = "UTF-8"

        // Custom options
        addBooleanOption("html5", true)
        addStringOption("-release", javaVersion)
        // Links to external javadocs
        links("https://docs.oracle.com/en/java/javase/$javaVersion/docs/api/")
        links("https://javadoc.io/doc/net.kyori/adventure-api/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/net.kyori/adventure-nbt/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/net.kyori/adventure-key/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/net.kyori/adventure-text-serializer-ansi/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/net.kyori/adventure-text-serializer-gson/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/net.kyori/adventure-text-serializer-legacy/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/net.kyori/adventure-text-serializer-plain/${libs.versions.adventure.get()}/")
        links("https://javadoc.io/doc/com.google.code.gson/gson/${libs.versions.gson.get()}/")
        links("https://javadoc.io/doc/org.jetbrains/annotations/${libs.versions.jetbrainsAnnotations.get()}/")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Viewable packets make tracking harder. Could be re-enabled later.
    jvmArgs("-Dminestom.viewable-packet=false")
    jvmArgs("-Dminestom.inside-test=true")
    jvmArgs("-Dminestom.acquirable-strict=true")
    minHeapSize = "512m"
    maxHeapSize = "1024m"
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
