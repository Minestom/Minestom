package net.minestom.server.extensions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
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
    static final String NAME_REGEX = "[A-Za-z][_A-Za-z0-9\\.]*[A-Za-z0-9]";
    // From https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
    static final String VERSION_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

    static @NotNull ExtensionDescriptor fromJson(@NotNull JsonObject json, @NotNull Path parentDirectory, @NotNull URL... classpath) {
        Check.argCondition(!json.has("name"), "Extensions must provide a name");
        String name = json.get("name").getAsString();

        Check.argCondition(!json.has("version"), "Extensions must provide a version");
        String version = json.get("version").getAsString();

        List<String> authors = new ArrayList<>();
        if (json.has("authors")) {
            JsonElement authorsElement = json.get("authors");
            if (authorsElement.isJsonArray()) {
                for (JsonElement author : authorsElement.getAsJsonArray()) {
                    Check.argCondition(!author.isJsonPrimitive(), "Authors must be strings, not: " + author);
                    authors.add(author.getAsString());
                }
            } else if (authorsElement.isJsonPrimitive()) {
                authors.add(authorsElement.getAsString());
            } else throw new IllegalArgumentException("Extension authors must be an array or single String.");
        }

        Check.argCondition(!json.has("entrypoint"), "Extensions must provide an entrypoint");
        String entrypoint = json.get("entrypoint").getAsString();

        List<Repository> repositories = new ArrayList<>();
        if (json.has("repositories")) {
            JsonElement repositoriesElement = json.get("repositories");
            Check.argCondition(!repositoriesElement.isJsonArray(), "Repositories must be an array, not: " + repositoriesElement);
            for (JsonElement repositoryElement : repositoriesElement.getAsJsonArray()) {
                Repository repository = Repository.fromJson(repositoryElement);
                if (repository != null) repositories.add(repository);
            }
        }

        List<Dependency> dependencies = new ArrayList<>();
        if (json.has("dependencies")) {
            JsonElement dependenciesElement = json.get("dependencies");
            Check.argCondition(!dependenciesElement.isJsonArray(), "Dependencies must be an array, not: " + dependenciesElement);
            for (JsonElement dependencyElement : dependenciesElement.getAsJsonArray()) {
                Dependency loaded = Dependency.fromJson(dependencyElement);
                if (loaded != null) dependencies.add(loaded);
            }
        }

        JsonObject meta = new JsonObject();
        if (json.has("meta")) {
            JsonElement metaElement = json.get("meta");
            Check.argCondition(!metaElement.isJsonObject(), "Extension meta must be an object, not: " + metaElement);
            meta = metaElement.getAsJsonObject();
        }

        return new ExtensionDescriptorImpl(
                name, version, authors, entrypoint,
                repositories, dependencies, meta,
                parentDirectory.resolve(name),
                new HierarchyClassLoader("Ext_" + name, classpath)
        );
    }

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
