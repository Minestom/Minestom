plugins {
    java
}

// Always exclude checker-qual. This is the single most annoying thing that always reappears.
configurations.all {
    // We only use Jetbrains Annotations
    exclude("org.checkerframework", "checker-qual")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}