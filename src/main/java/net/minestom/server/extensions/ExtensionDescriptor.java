package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface ExtensionDescriptor {

    static ExtensionDescriptor newDescriptor(
            @NotNull String name,
            @NotNull String version,
            @NotNull List<String> authors,
            @NotNull String entrypoint,
            @NotNull List<Repository> repositories,
            @NotNull List<Dependency> dependencies,
            @NotNull JsonObject meta,
            @NotNull Path dataDirectory,
            @NotNull HierarchyClassLoader classLoader) {
        return new ExtensionDescriptorImpl(name, version, authors, entrypoint, repositories, dependencies, meta, dataDirectory, classLoader);
    }

    //todo docs, reader is **not** closed
    static @NotNull ExtensionDescriptor fromReader(@NotNull Reader reader, @NotNull Path parentDirectory, @NotNull URL... classpath) {
        return fromJson(JsonParser.parseReader(reader).getAsJsonObject(), parentDirectory, classpath);
    }

    static @NotNull ExtensionDescriptor fromJson(@NotNull JsonObject json, @NotNull Path parentDirectory, @NotNull URL... classpath) {
        return ExtensionDescriptorImpl.fromJson(json, parentDirectory, classpath);
    }

    @NotNull String name();

    @NotNull String version();

    @NotNull List<String> authors();

    @NotNull String entrypoint();

    @NotNull List<Repository> repositories();

    @NotNull List<Dependency> dependencies();

    @NotNull JsonObject meta();

    @NotNull Path dataDirectory();

    @NotNull HierarchyClassLoader classLoader();
}
