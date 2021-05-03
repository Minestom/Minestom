plugins {
    id("application")
}

application {
    mainClass.set("com.minestom.data_generator.DataGenerator")
}

dependencies {
    implementation(project(":DataGenerator:core"))
    compileOnly(files("../../Deobfuscator/deobfuscated_jars/deobfu_21w15a.jar"))

    val version: String = (project.properties["mcversion"] ?: "1.16.5") as String
    runtimeOnly(files("../../Deobfuscator/deobfuscated_jars/deobfu_$version.jar"))
}
