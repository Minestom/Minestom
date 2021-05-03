import java.util.*
import kotlin.math.log

group = "com.minestom"
version = "0.1.0"

subprojects {
    version = project.rootProject.version
}

tasks {
    register("generateData") {
        val version: String = (project.properties["mcversion"] ?: "1.16.5") as String
        val outputLocation: String = (project.properties["output"] ?: "output") as String
        val closestVersion: String = getClosestVersion(version)

        logger.info("Mojang requires all source-code and mappings used to be governed by the Minecraft EULA.")
        logger.info("Please read the Minecraft EULA located at https://account.mojang.com/documents/minecraft_eula.")
        logger.info("In order to agree to the EULA you must create a file called eula.txt with the text 'eula=true'.")
        val eulaTxt = File("${project.rootProject.projectDir}/eula.txt")
        logger.info("The file must be located at '${eulaTxt.absolutePath}'.")
        if (eulaTxt.exists() && eulaTxt.readText(Charsets.UTF_8).equals("eula=true", true)) {
            logger.info("The EULA has been accepted and signed.")
        } else {
            throw GradleException("Data generation has been halted as the EULA has not been signed.")
        }
        logger.info("It is unclear if the data from the data generator also adhere to the Minecraft EULA.")
        logger.info("Please consult your own legal team!")
        logger.info("All data is given independently without warranty, guarantee or liability of any kind.")
        logger.info("The data may or may not be the intellectual property of Mojang Studios.")

        // DataGeneration
        val projectDG: Project = project(":DataGenerator:$closestVersion")
        dependsOn(projectDG.tasks.getByName<JavaExec>("run") {
            args = arrayListOf(version, outputLocation)
            // Deobfuscation
            run {
                if (version != closestVersion) {
                    // Need to run deobfuscator on closestVersion as it is used to compile.
                    // This just prevents us from running the Deobfuscator on the same version twice
                    dependsOn(project(":Deobfuscator").tasks.getByName<JavaExec>("run") {
                        setArgsString(closestVersion)
                    })
                }
                // Need to run deobfuscator on version as it is used on runtime.
                dependsOn(project(":Deobfuscator").tasks.getByName<JavaExec>("run") {
                    setArgsString(version)
                })
            }
        })
    }
}

fun getClosestVersion(version: String): String {
    // check if the specified version exists as a project
    try {
        // e.g. if the project 21w15a exists then this WONT throw the UnknownProjectException and return "21w15a".
        val projectC : Project = project(":DataGenerator:$version")
        return version
    } catch (e : UnknownProjectException) {
        // ignored
    }
    // TODO: Get closest version and not use a hardfixed version
    // UPDATE: Update this version if we haven't done created a function to get the closest version.
    return "1.16.5"
}

