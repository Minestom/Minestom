package net.minestom.server.extensions.descriptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.extensions.HierarchyClassLoader;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface ExtensionDescriptor {

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