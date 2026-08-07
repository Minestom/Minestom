plugins {
    id("minestom.java-binary")
}

dependencies {
    implementation(project(":"))

    runtimeOnly(libs.bundles.logback)
}

application {
    mainClass.set("net.minestom.demo.Main")
    mainModule.set("net.minestom.demo")

    applicationDefaultJvmArgs += "-ea"
}
