package net.minestom.server.extensions.descriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.extensions.DiscoveredExtension;
import net.minestom.server.extensions.ExtensionClassLoader;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
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

        @NotNull Path dataDirectory,
        @NotNull ExtensionClassLoader classLoader
) {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveredExtension.class);

    static final String NAME_REGEX = "[A-Za-z][_A-Za-z0-9\\.]*[A-Za-z0-9]";
    // From https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
    static final String VERSION_REGEX = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

    public ExtensionDescriptor {
        //todo test me
        Check.argCondition(!name.matches(NAME_REGEX), "Invalid extension name: " + name);
        Check.argCondition(!version.matches(VERSION_REGEX), "Invalid extension version: " + version + ". Semantic versioning must be followed. For more information visit https://semver.org/.");
        authors = List.copyOf(authors);

        Check.argCondition(entrypoint.isEmpty(), "An extension must have a valid entrypoint.");
        repositories = List.copyOf(repositories);
        dependencies = List.copyOf(dependencies);

        // meta

        dataDirectory = dataDirectory.toAbsolutePath();
        //todo classloader
    }

    //todo docs, reader is **not** closed
    @NotNull
    public static ExtensionDescriptor fromReader(@NotNull Reader reader, @NotNull Path parentDirectory, @NotNull URL... classpath) {
        return fromJson(JsonParser.parseReader(reader).getAsJsonObject(), parentDirectory, classpath);
    }

    @NotNull
    public static ExtensionDescriptor fromJson(@NotNull JsonObject json, @NotNull Path parentDirectory, @NotNull URL... classpath) {
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

        }

        List<Dependency> dependencies = new ArrayList<>();
        if (json.has("dependencies")) {

        }

        JsonObject meta = new JsonObject();
        if (json.has("meta")) {
            JsonElement metaElement = json.get("meta");
            Check.argCondition(metaElement.isJsonObject(), "Extension meta must be an object");
            meta = metaElement.getAsJsonObject();
        }

        //todo classloader

        return new ExtensionDescriptor(
                name, version, authors, entrypoint,
                repositories, dependencies, meta,
                parentDirectory.resolve(name),
                null
        );
    }
}
