plugins {
    id("minestom.java-binary")
}

dependencies {
    implementation(rootProject)

    runtimeOnly(libs.bundles.logback)
}

application {
    mainModule.set("net.minestom.demo")
    mainClass.set("net.minestom.demo.Main")
    mainModule.set("net.minestom.demo")

    applicationDefaultJvmArgs += "-ea"
}
