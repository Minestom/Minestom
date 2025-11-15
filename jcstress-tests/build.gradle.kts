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
    jcstressDependency = "org.openjdk.jcstress:jcstress-core:${libs.versions.jcstress.asProvider().get()}"
    jvmArgs = "--enable-native-access=ALL-UNNAMED"
}
