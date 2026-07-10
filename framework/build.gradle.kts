plugins {
    id("minestom.java-library")
    id("minestom.publishing")
    alias(libs.plugins.blossom)
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("COMMIT", System.getenv("GITHUB_SHA") ?: "LOCAL")
                property("BRANCH", System.getenv("GITHUB_REF") ?: "LOCAL")
                property("GROUP", project.group.toString())
                property("ARTIFACT", rootProject.name) // the published aggregate artifact
                property("VERSION", project.version.toString())
            }
        }
    }
}

// Classpath jar: shares net.minestom.server.* packages with :lib (see :lib notes).
java {
    modularity.inferModulePath = false
}

dependencies {
    api(project(":lib"))
    implementation(libs.slf4j)
    implementation(libs.fastutil)
    implementation(libs.bundles.flare)
    implementation(libs.jcTools)
}
