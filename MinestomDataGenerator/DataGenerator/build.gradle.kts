group = "com.minestom.data_generator"

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }
    dependencies {
        val compileOnly by configurations
        val implementation by configurations

        // Jetbrains annotations
        compileOnly("org.jetbrains:annotations:20.1.0")
        // Logging
        implementation("org.apache.logging.log4j:log4j-core:2.14.0")
        // SLF4J is the base logger for most libraries, therefore we can hook it into log4j2.
        implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
        // Gson
        implementation("com.google.code.gson:gson:2.8.6")
    }
    configure<JavaPluginConvention> {
        sourceSets {
            getByName("test") {
                java {
                    setSrcDirs(emptyList<Any?>())
                }
                resources {
                    setSrcDirs(emptyList<Any?>())
                }
            }
        }
    }
    tasks {
        getByName<Test>("test") {
            enabled = false
        }
    }
}