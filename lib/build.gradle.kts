plugins {
    `java-library`

    `maven-publish`
    signing
}

group = "net.minestom"
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    // Core dependencies
    implementation(libs.minestomData)
    api(libs.jetbrainsAnnotations)
    api(libs.bundles.adventure)

    // Performance/data structures
    implementation(libs.caffeine)
    api(libs.fastutil)
    api(libs.gson)
    implementation(libs.jcTools)

    // Testing
    testImplementation(libs.bundles.junit)
}

tasks.test {
    useJUnitPlatform()
}

publishing.publications.create<MavenPublication>("lib") {
    groupId = project.group.toString()
    artifactId = "minestom-lib-snapshots" // todo: decide on publishing scheme
    version = project.version.toString()

    from(project.components["java"])

    pom {
        name.set(this@create.artifactId)

        val commonPomConfig: Action<MavenPom> by project.extra
        commonPomConfig.execute(this)
    }
}

signing {
    isRequired = System.getenv("CI") != null

    val privateKey = System.getenv("GPG_PRIVATE_KEY")
    val keyPassphrase = System.getenv()["GPG_PASSPHRASE"]
    useInMemoryPgpKeys(privateKey, keyPassphrase)

    sign(publishing.publications)
}
