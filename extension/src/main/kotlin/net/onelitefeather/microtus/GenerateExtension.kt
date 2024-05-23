package net.onelitefeather.microtus

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.onelitefeather.microtus.models.Extension
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import kotlin.jvm.Throws

/**
 * Defines the task which generates the required extension.json file for Minestom.
 * @since 1.2.0
 * @author TheMeinerLP
 */
abstract class GenerateExtension : DefaultTask() {
    @get:Input
    abstract val fileName: Property<String>

    @get:Nested
    abstract val extension: Property<Extension>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val librariesRootComponent: Property<ResolvedComponentResult>

    /**
     * Generates an extension.json file based on the given properties.
     */
    @TaskAction
    fun generate() {
        val module = SimpleModule()
        val mapper = ObjectMapper()
                .registerKotlinModule()
                .registerModule(module)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        val extension = extension.get()
        val dependencies = librariesRootComponent.orNull.collectLibraries()
        val repos = this.project.repositories.withType(MavenArtifactRepository::class.java)
        val externalDependencies = Extension.ExternalDependencies()
        externalDependencies.artifacts = dependencies.toList()
        val mavenCentral = Extension.Repository()
        mavenCentral.url = "https://repo.maven.apache.org/maven2/"
        mavenCentral.name = "mavenCentral"
        val mappedRepos = repos.map {
            val repo = Extension.Repository()
            repo.url = it.url.toString()
            repo.name = it.name
            repo
        }.toList()
        externalDependencies.repositories = if (mappedRepos.isNotEmpty() || dependencies.isNotEmpty()) {
            mappedRepos + listOf(mavenCentral)
        } else {
            listOf()
        }
        extension.externalDependencies = externalDependencies
        mapper.writeValue(outputDirectory.file(fileName).get().asFile, extension)
    }

    /**
     * Collects all libraries from the root component and the additional libraries.
     * @param additional the additional libraries to add
     */
    private fun ResolvedComponentResult?.collectLibraries(additional: List<String>? = null): List<String> {
        val resolved = this?.dependencies?.map { dependencyResult ->
            (dependencyResult as? ResolvedDependencyResult)?.selected?.moduleVersion?.toString() ?: error("No moduleVersion for $dependencyResult")
        }
        return ((additional ?: listOf()) + (resolved ?: listOf())).distinct()
    }
}