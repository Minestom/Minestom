package net.minestom.server.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public abstract class Extension {
    // Set by reflection
    @SuppressWarnings("unused")
    private ExtensionDescription description;
    // Set by reflection
    @SuppressWarnings("unused")
    private Logger logger;
    // Set by reflection
    @SuppressWarnings("unused")
    private Path dataDirectory;

    protected Extension() {

    }

    public void preInitialize() {

    }

    public abstract void initialize();

    public void postInitialize() {

    }

    public void preTerminate() {

    }

    public abstract void terminate();

    public void postTerminate() {

    }

    /**
     * Called after postTerminate when reloading an extension
     */
    public void unload() {

    }

    @NotNull
    public ExtensionDescription getDescription() {
        return description;
    }

    @NotNull
    protected Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     * <p>
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    @Nullable
    public InputStream getResource(@NotNull String fileName) {
        Path targetFile = getDataDirectory().resolve(fileName);
        try {
            // Copy from jar if the file does not exist in extension directory
            if (!Files.exists(targetFile))
                savePackagedResource(fileName);

            return Files.newInputStream(targetFile);
        } catch (IOException ex) {
            getLogger().debug("Failed to read resource {}.", fileName, ex);
            return null;
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    @Nullable
    public InputStream getPackagedResource(@NotNull String fileName) {
        try {
            URL url = getClass().getClassLoader().getResource(fileName);
            if (url == null) {
                getLogger().debug("Resource not found: {}", fileName);
                return null;
            }

            return url.openConnection().getInputStream();
        } catch (IOException ex) {
            getLogger().debug("Failed to load resource {}.", fileName, ex);
            return null;
        }
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param fileName The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    public boolean savePackagedResource(@NotNull String fileName) {
        Path targetFile = getDataDirectory().resolve(fileName);
        try (InputStream is = getPackagedResource(fileName)) {
            Files.createDirectories(targetFile.getParent());

            if (is == null)
                return false;

            Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            getLogger().info("Failed to save resource {}.", fileName, ex);
            return false;
        }
    }

    public static class ExtensionDescription {
        private final String name;
        private final String version;
        private final List<String> authors;
        private final List<String> dependents = new ArrayList<>();
        private final DiscoveredExtension origin;

        ExtensionDescription(@NotNull String name, @NotNull String version, @NotNull List<String> authors, @NotNull DiscoveredExtension origin) {
            this.name = name;
            this.version = version;
            this.authors = authors;
            this.origin = origin;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public String getVersion() {
            return version;
        }

        @NotNull
        public List<String> getAuthors() {
            return authors;
        }

        @NotNull
        public List<String> getDependents() {
            return dependents;
        }

        @NotNull
        DiscoveredExtension getOrigin() {
            return origin;
        }
    }
}
