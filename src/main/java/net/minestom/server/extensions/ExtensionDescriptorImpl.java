package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

record ExtensionDescriptorImpl(
        @NotNull String name,
        @NotNull String version,
        @NotNull List<String> authors,

        @NotNull String entrypoint,
        @NotNull List<Repository> repositories,
        @NotNull List<Dependency> dependencies,

        @NotNull JsonObject meta,

        @NotNull Path dataDirectory,
        @NotNull HierarchyClassLoader classLoader
) implements ExtensionDescriptor {
    ExtensionDescriptorImpl {
        Check.argCondition(!name.matches(NAME_REGEX), "Invalid extension name: " + name);
        Check.argCondition(!version.matches(VERSION_REGEX), "Invalid extension version: " + version + ". Semantic versioning must be followed. For more information visit https://semver.org/.");
        authors = List.copyOf(authors);

        Check.argCondition(entrypoint.isEmpty(), "An extension must have a valid entrypoint.");
        repositories = List.copyOf(repositories);
        dependencies = List.copyOf(dependencies);

        dataDirectory = dataDirectory.toAbsolutePath();
    }
}
