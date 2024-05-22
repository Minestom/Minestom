plugins {
    id("io.github.reyerizo.gradle.jcstress") version "0.8.15"
    id("minestom.common-conventions")
}

dependencies {
    jcstressImplementation(rootProject)
    jcstress(libs.jcstress.core)
}

jcstress {
    verbose = "true"
}
