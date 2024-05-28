dependencyResolutionManagement {
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        maven("https://s01.oss.sonatype.org/content/groups/public/")
        maven("https://jitpack.io")
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            // Important dependencies
            version("adventure", "4.15.0")
            version("kotlin", "1.9.22")
            version("hydrazine", "1.7.2")
            version("data", "1.20.4-rv10")
            version("hephaistos", "2.6.1")
            version("jetbrainsAnnotations", "24.1.0")
            version("logback", "1.4.5")
            version("slf4j", "2.0.7")
            version("maven-resolver", "1.9.18")
            version("maven-resolver-provider", "3.9.6")

            // Terminal / Logging
            version("tinylog", "2.6.2")
            version("jline", "3.25.1")

            // Performance / Data Structures
            version("caffeine", "3.1.8")
            version("fastutil", "8.5.12")
            version("flare", "2.0.1")
            version("gson", "2.10.1")
            version("jcTools", "4.0.2")

            // Test
            version("junit-jupiter", "5.10.1")
            version("junit-platform", "1.10.1")
            version("mockito", "5.9.0")

            // Code Generation
            version("javaPoet", "1.13.0")

            // Demo
            version("jNoise", "b93008e35e")

            // JMH
            version("jmh", "1.37")

            // JCStress
            version("jcstress", "0.16")

            // Gradle plugins
            version("blossom", "2.1.0")

            // BStats
            version("bstats", "3.0.2")

            // Libs
            library("adventure-api", "net.kyori", "adventure-api").versionRef("adventure")
            library("adventure-serializer-gson", "net.kyori", "adventure-text-serializer-gson").versionRef("adventure")
            library("adventure-serializer-legacy", "net.kyori", "adventure-text-serializer-legacy").versionRef("adventure")
            library("adventure-serializer-plain", "net.kyori", "adventure-text-serializer-plain").versionRef("adventure")
            library("adventure-text-logger-slf4j", "net.kyori", "adventure-text-logger-slf4j").versionRef("adventure")
            library("adventure-mini-message", "net.kyori", "adventure-text-minimessage").versionRef("adventure")

            // Maven
            library("maven.connector", "org.apache.maven.resolver", "maven-resolver-connector-basic").versionRef("maven-resolver")
            library("maven.transport.http", "org.apache.maven.resolver", "maven-resolver-transport-http").versionRef("maven-resolver")
            library("maven.resolver", "org.apache.maven", "maven-resolver-provider").versionRef("maven-resolver-provider")

            // Kotlin
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            library("kotlin-stdlib-jdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")

            // Miscellaneous
            library("hydrazine", "com.github.MadMartian", "hydrazine-path-finding").versionRef("hydrazine")
            library("minestomData", "net.onelitefeather.microtus", "data").versionRef("data")
            library("jetbrainsAnnotations", "org.jetbrains", "annotations").versionRef("jetbrainsAnnotations");

            // Logging
            library("tinylog-api", "org.tinylog", "tinylog-api").versionRef("tinylog")
            library("tinylog-impl", "org.tinylog", "tinylog-impl").versionRef("tinylog")
            library("tinylog-slf4j", "org.tinylog", "slf4j-tinylog").versionRef("tinylog")
            library("slf4j", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("logback-core", "ch.qos.logback", "logback-core").versionRef("logback")
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef("logback")

            // Terminal
            library("jline", "org.jline", "jline").versionRef("jline")
            library("jline-jansi", "org.jline", "jline-terminal-jansi").versionRef("jline")

            // Performance / Data Structures
            library("caffeine", "com.github.ben-manes.caffeine", "caffeine").versionRef("caffeine")
            library("fastutil", "it.unimi.dsi", "fastutil").versionRef("fastutil")
            library("flare", "space.vectrix.flare", "flare").versionRef("flare")
            library("flare-fastutil", "space.vectrix.flare", "flare-fastutil").versionRef("flare")
            library("gson", "com.google.code.gson", "gson").versionRef("gson")
            library("jcTools", "org.jctools", "jctools-core").versionRef("jcTools")

            // Tests
            library("junit-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit-jupiter")
            library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit-jupiter")
            library("junit-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit-jupiter")
            library("junit-suite-api", "org.junit.platform", "junit-platform-suite-api").versionRef("junit-platform")
            library("junit-suite-engine", "org.junit.platform", "junit-platform-suite-engine").versionRef("junit-platform")
            library("mockito-core", "org.mockito", "mockito-core").versionRef("mockito")

            // Code Generation
            library("javaPoet", "com.squareup", "javapoet").versionRef("javaPoet")

            // Demo
            library("jNoise", "com.github.Articdive.JNoise", "jnoise-pipeline").versionRef("jNoise")

            // JMH
            library("jmh-core", "org.openjdk.jmh", "jmh-core").versionRef("jmh")
            library("jmh-annotationprocessor", "org.openjdk.jmh", "jmh-generator-annprocess").versionRef("jmh")

            // JCStress
            library("jcstress-core", "org.openjdk.jcstress", "jcstress-core").versionRef("jcstress")

            // BStats
            library("bstats-base", "org.bstats", "bstats-base").versionRef("bstats")

            bundle("kotlin", listOf("kotlin-stdlib-jdk8", "kotlin-reflect"))
            bundle("flare", listOf("flare", "flare-fastutil"))
            bundle("adventure", listOf("adventure-api", "adventure-mini-message", "adventure-serializer-gson", "adventure-serializer-legacy", "adventure-serializer-plain", "adventure-text-logger-slf4j"))
            bundle("logging", listOf("tinylog-api", "tinylog-impl", "tinylog-slf4j"))
            bundle("terminal", listOf("jline", "jline-jansi"))
            bundle("logback", listOf("logback-core", "logback-classic"))
            bundle("junit", listOf("junit-api", "junit-engine", "junit-params", "junit-suite-api", "junit-suite-engine"))

            plugin("blossom", "net.kyori.blossom").versionRef("blossom")

        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://files.minecraftforge.net/maven/")
    }
    includeBuild("build-logic")
    includeBuild("extension")
}

rootProject.name = "Microtus"
include("code-generators")
include("jmh-benchmarks")
include("jcstress-tests")
include("demo")
include("testing")
include("bom")
