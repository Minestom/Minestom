plugins {
    id("minestom.java-library")
    alias(libs.plugins.jcstress.plugin)
}

dependencies {
    jcstressImplementation(rootProject)
    jcstress(libs.jcstress.core)
}

jcstress {
    verbose = "true"
}
