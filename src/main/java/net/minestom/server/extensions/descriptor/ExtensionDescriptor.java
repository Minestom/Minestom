package net.minestom.server.extensions.descriptor;

import com.google.gson.JsonObject;
import net.minestom.server.extensions.DiscoveredExtension;
import net.minestom.server.extensions.ExtensionClassLoader;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 *
 */
public record ExtensionDescriptor(
        @NotNull String name,
        @NotNull String version,
        @NotNull List<String> authors,

        @NotNull String entrypoint,
        @NotNull List<Repository> repositories,
        @NotNull List<Dependency> dependencies,

        @NotNull JsonObject meta,

        @NotNull String[] files, //todo
        @Nullable Path file,

        @NotNull Path dataDirectory,
        @NotNull ExtensionClassLoader classLoader //todo

        ) {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveredExtension.class);

    static final String NAME_REGEX = "[A-Za-z][_A-Za-z0-9]+";
    // From https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
    static final String VERSION_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

    public ExtensionDescriptor {
        //todo test me
        Check.argCondition(name.matches(NAME_REGEX), "Invalid extension name: " + name);
        Check.argCondition(version.matches(VERSION_REGEX), "Invalid extension version: " + version + ". Semantic versioning must be followed. For more information visit https://semver.org/.");
        authors = List.copyOf(authors);

        Check.argCondition(entrypoint.isEmpty(), "An extension must have a valid entrypoint.");
        repositories = List.copyOf(repositories);
        dependencies = List.copyOf(dependencies);

        //todo files
        //todo file

        dataDirectory = dataDirectory.toAbsolutePath();
        //todo classloader
    }
}
