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

tasks.register<JavaExec>("runScratch") {
    classpath = sourceSets["scratch"].runtimeClasspath
    mainClass.set("net.minestom.demo.Scratch")
}

application {
    mainClass.set("net.minestom.demo.Main")
    mainModule.set("net.minestom.demo")

    applicationDefaultJvmArgs += "-ea"
}
