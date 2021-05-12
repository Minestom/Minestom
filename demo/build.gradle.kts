description = "An example Minestom implementation for testing and demo purposes"

plugins {
    // application - produce an executable application
    application

    // shadow - produce a jar with dependencies
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

application {
    mainClass.set("net.minestom.demo.Main")
}

dependencies {
    implementation(projects.minestomCore)
    implementation(projects.minestomExtrasLwjgl)
}

// prevent publishing the demo
tasks.withType<AbstractPublishToMaven>().configureEach {
    onlyIf { false }
}