package net.minestom.server.extensions;

import com.google.gson.JsonObject;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an extension from an `extension.json` that is capable of powering an Extension object.
 *
 * This has no constructor as its properties are set via GSON.
 */
public final class DiscoveredExtension {
    /** Static logger for this class. */
    public static final Logger LOGGER = LoggerFactory.getLogger(DiscoveredExtension.class);

    /** The regex that this name must pass. If it doesn't, it will not be accepted. */
    public static final String NAME_REGEX = "[A-Za-z][_A-Za-z0-9]+";

    /** Name of the DiscoveredExtension. Unique for all extensions. */
    private String name;

    /** Main class of this DiscoveredExtension, must extend Extension. */
    private String entrypoint;

    /** Version of this extension, highly reccomended to set it. */
    private String version;

    /** People who have made this extension. */
    private String[] authors;

    /** List of extension names that this depends on. */
    private String[] dependencies;

    /** List of Repositories and URLs that this depends on. */
    private ExternalDependencies externalDependencies;

    /**
     * Extra meta on the object.
     * Do NOT use as configuration:
     *
     * Meta is meant to handle properties that will
     * be accessed by other extensions, not accessed by itself
     */
    private JsonObject meta;

    /** All files of this extension */
    transient List<URL> files = new LinkedList<>();

    /** The load status of this extension -- LOAD_SUCCESS is the only good one. */
    transient LoadStatus loadStatus = LoadStatus.LOAD_SUCCESS;

    /** The original jar this is from. */
    transient private File originalJar;

    transient private Path dataDirectory;

    /** The class loader that powers it. */
    transient private ExtensionClassLoader classLoader;

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getEntrypoint() {
        return entrypoint;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @NotNull
    public String[] getAuthors() {
        return authors;
    }

    @NotNull
    public String[] getDependencies() {
        return dependencies;
    }

    @NotNull
    public ExternalDependencies getExternalDependencies() {
        return externalDependencies;
    }

    public void setOriginalJar(@Nullable File file) {
        originalJar = file;
    }

    @Nullable
    public File getOriginalJar() {
        return originalJar;
    }

    public @NotNull Path getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(@NotNull Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    void createClassLoader() {
        Check.stateCondition(classLoader != null, "Extension classloader has already been created");
        final URL[] urls = this.files.toArray(new URL[0]);
        classLoader = new ExtensionClassLoader(this.getName(), urls);
    }

    @NotNull
    public ExtensionClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Ensures that all properties of this extension are properly set if they aren't
     *
     * @param extension The extension to verify
     */
    public static void verifyIntegrity(@NotNull DiscoveredExtension extension) {
        if (extension.name == null) {
            StringBuilder fileList = new StringBuilder();
            for (URL f : extension.files) {
                fileList.append(f.toExternalForm()).append(", ");
            }
            LOGGER.error("Extension with no name. (at {}})", fileList);
            LOGGER.error("Extension at ({}) will not be loaded.", fileList);
            extension.loadStatus = DiscoveredExtension.LoadStatus.INVALID_NAME;

            // To ensure @NotNull: name = INVALID_NAME
            extension.name = extension.loadStatus.name();
            return;
        }

        if (!extension.name.matches(NAME_REGEX)) {
            LOGGER.error("Extension '{}' specified an invalid name.", extension.name);
            LOGGER.error("Extension '{}' will not be loaded.", extension.name);
            extension.loadStatus = DiscoveredExtension.LoadStatus.INVALID_NAME;

            // To ensure @NotNull: name = INVALID_NAME
            extension.name = extension.loadStatus.name();
            return;
        }

        if (extension.entrypoint == null) {
            LOGGER.error("Extension '{}' did not specify an entry point (via 'entrypoint').", extension.name);
            LOGGER.error("Extension '{}' will not be loaded.", extension.name);
            extension.loadStatus = DiscoveredExtension.LoadStatus.NO_ENTRYPOINT;

            // To ensure @NotNull: entrypoint = NO_ENTRYPOINT
            extension.entrypoint = extension.loadStatus.name();
            return;
        }

        // Handle defaults
        // If we reach this code, then the extension will most likely be loaded:
        if (extension.version == null) {
            LOGGER.warn("Extension '{}' did not specify a version.", extension.name);
            LOGGER.warn("Extension '{}' will continue to load but should specify a plugin version.", extension.name);
            extension.version = "Unspecified";
        }

        if (extension.authors == null) {
            extension.authors = new String[0];
        }

        // No dependencies were specified
        if (extension.dependencies == null) {
            extension.dependencies = new String[0];
        }

        // No external dependencies were specified;
        if (extension.externalDependencies == null) {
            extension.externalDependencies = new ExternalDependencies();
        }

        // No meta was provided
        if (extension.meta == null) {
            extension.meta = new JsonObject();
        }

    }

    @NotNull
    public JsonObject getMeta() {
        return meta;
    }

    /**
     * The status this extension has, all are breakpoints.
     *
     * LOAD_SUCCESS is the only valid one.
     */
    enum LoadStatus {
        LOAD_SUCCESS("Actually, it did not fail. This message should not have been printed."),
        MISSING_DEPENDENCIES("Missing dependencies, check your logs."),
        INVALID_NAME("Invalid name."),
        NO_ENTRYPOINT("No entrypoint specified."),
        FAILED_TO_SETUP_CLASSLOADER("Extension classloader could not be setup."),
        LOAD_FAILED("Load failed. See logs for more information."),
        ;

        private final String message;

        LoadStatus(@NotNull String message) {
            this.message = message;
        }

        @NotNull
        public String getMessage() {
            return message;
        }
    }

    public static final class ExternalDependencies {
        Repository[] repositories = new Repository[0];
        String[] artifacts = new String[0];

        public static class Repository {
            String name = "";
            String url = "";
        }
    }
}
