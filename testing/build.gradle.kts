plugins {
    `java-library`
//    `maven-publish`
}

group = "net.minestom.testing"
// version declared by root project

dependencies {
    api(rootProject)

    api(libs.junit.api)
    api(libs.junit.params)
    api(libs.junit.suite.api)
    runtimeOnly(libs.junit.engine)
    runtimeOnly(libs.junit.suite.engine)
}

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = "net.minestom.testing"
//            artifactId = "testing"
//            version = "1.0"
//
//            from(components["java"])
//        }
//    }
//}
