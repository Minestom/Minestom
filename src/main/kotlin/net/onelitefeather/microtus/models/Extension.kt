package net.onelitefeather.microtus.models

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the allowed data fields for an extension.json file which is required from Minestom to load an extension.
 * The following fields are allowed (fields with a * are required):
 *
 * - name: The name of the extension (*)
 * - version: The version of the extension (*)
 * - entrypoint: The entrypoint of the extension (*)
 * - authors: The authors of the extension
 * - dependencies: The dependencies of the extension
 * - externalDependencies: The external dependencies of the extension
 * @since 1.2.0
 * @author TheMeinerLP
 */
class Extension() {
    @Input
    var name: String? = null

    @Input
    var version: String? = null

    @Input
    var entrypoint: String? = null

    @Input
    @Optional
    @JsonProperty("authors")
    var authors: List<String>? = null

    @Input
    @Optional
    @JsonProperty("dependencies")
    var dependencies: List<String>? = null

    @Input
    @Optional
    @JsonProperty("externalDependencies")
    var externalDependencies: ExternalDependencies? = null

    /**
     * Represents the external dependencies which is required for the extension to work.
     * @since 1.2.0
     * @author TheMeinerLP
     */
    class ExternalDependencies {
        @JsonProperty("repositories")
        var repositories: List<Repository>? = null

        @Input
        @Optional
        @JsonProperty("artifacts")
        var artifacts: List<String>? = null

    }

    /**
     * Represents a repository which is used by an extension to load external dependencies.
     * @since 1.2.0
     * @author TheMeinerLP
     * @see ExternalDependencies
     */
    class Repository {
        @Input
        @JsonProperty("name")
        var name: String? = null

        @Input
        @JsonProperty("url")
        var url: String? = null
    }
}
