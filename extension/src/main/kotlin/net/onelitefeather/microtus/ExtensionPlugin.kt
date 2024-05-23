package net.onelitefeather.microtus

import net.onelitefeather.microtus.models.Extension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/**
 * Represents the gradle plugin extension which can be integrated into a `build.gradle.kts` file to define the properties of the extension.
 * The usage of this plugin is as follows:
 *
 * ```kotlin
 * plugins {
 *    id("net.onelitefeather.microtus.extension") version "<version>"
 *    // ...
 * }
 *
 * extension {
 *  //... use the properties of the extension here
 * }
 * ```
 *
 * @since 1.2.0
 * @author TheMeinerLP
 */
class ExtensionPlugin : Plugin<Project> {

    private val fileName = "extension.json"

    /**
     * Applies the plugin to the project.
     * @param target the project to apply the plugin to
     */
    override fun apply(target: Project) {
        target.run {
            val generatedResourcesDirectory = layout.buildDirectory.dir("generated/extension")
            val extension = Extension()
            extensions.add("extension", extension)

            val library = project.configurations.maybeCreate("library")
            val libraries = project.configurations.create("extensionLibrary").extendsFrom(library)

            val generateTask = tasks.register<GenerateExtension>("generateExtension") {
                group = "minestom"

                fileName.set(this@ExtensionPlugin.fileName)
                librariesRootComponent.set(libraries.incoming.resolutionResult.root)
                outputDirectory.set(generatedResourcesDirectory)
                this.extension.set(provider {
                    setDefaults(project, extension)
                    extension
                })
            }
            plugins.withType<JavaPlugin> {
                extensions.getByType<SourceSetContainer>().named(SourceSet.MAIN_SOURCE_SET_NAME) {
                    resources.srcDir(generateTask)
                }
            }
        }
    }

    /**
     * Sets some default variables to a [Extension] object.
     * @param project the project to get the name and version from
     * @param extension the extension to set the variables to
     */
    private fun setDefaults(project: Project, extension: Extension) {
        extension.name = extension.name ?: project.name
        extension.version = extension.version ?: project.version.toString()
    }
}