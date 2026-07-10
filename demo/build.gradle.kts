plugins {
    id("minestom.java-binary")
}

dependencies {
    implementation(rootProject)

    runtimeOnly(libs.bundles.logback)
}

// Scratch compiles against :lib alone, proving the unopinionated slice is self-contained.
// A stray framework import there must fail the build instead of resolving via the aggregate.
sourceSets {
    create("scratch")
}

dependencies {
    "scratchImplementation"(project(":lib"))
}

tasks.named("check") {
    dependsOn(tasks.named("compileScratchJava"))
}

application {
    mainClass.set("net.minestom.demo.Main")
    mainModule.set("net.minestom.demo")

    applicationDefaultJvmArgs += "-ea"
}
