plugins {
    id("io.github.reyerizo.gradle.jcstress") version "0.8.15"
}

dependencies {
    jcstressImplementation(rootProject)
    jcstress(libs.jcstress.core)
}

jcstress {
    verbose = "true"
}
