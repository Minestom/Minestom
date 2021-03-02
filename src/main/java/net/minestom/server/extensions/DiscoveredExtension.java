package net.minestom.server.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

final class DiscoveredExtension {

    public final static Logger LOGGER = LoggerFactory.getLogger(DiscoveredExtension.class);

    public static final String NAME_REGEX = "[A-Za-z][_A-Za-z0-9]+";
    private String name;
    private String entrypoint;
    private String version;
    private String mixinConfig;
    private String[] authors;
    private String[] codeModifiers;
    private String[] dependencies;
    private ExternalDependencies externalDependencies;
    private List<String> missingCodeModifiers = new LinkedList<>();
    private boolean failedToLoadMixin = false;
    transient List<URL> files = new LinkedList<>();
    transient LoadStatus loadStatus = LoadStatus.LOAD_SUCCESS;
    transient private File originalJar;

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
    public String getMixinConfig() {
        return mixinConfig;
    }

    @NotNull
    public String[] getAuthors() {
        return authors;
    }

    @NotNull
    public String[] getCodeModifiers() {
        if (codeModifiers == null) {
            codeModifiers = new String[0];
        }
        return codeModifiers;
    }

    @NotNull
    public String[] getDependencies() {
        return dependencies;
    }

    @NotNull
    public ExternalDependencies getExternalDependencies() {
        return externalDependencies;
    }

    void setOriginalJar(@Nullable File file) {
        originalJar = file;
    }

    @Nullable
    File getOriginalJar() {
        return originalJar;
    }

    static void verifyIntegrity(@NotNull DiscoveredExtension extension) {
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
        if (extension.mixinConfig == null) {
            extension.mixinConfig = "";
        }
        if (extension.authors == null) {
            extension.authors = new String[0];
        }
        if (extension.codeModifiers == null) {
            extension.codeModifiers = new String[0];
        }
        // No dependencies were specified
        if (extension.dependencies == null) {
            extension.dependencies = new String[0];
        }
        // No external dependencies were specified;
        if (extension.externalDependencies == null) {
            extension.externalDependencies = new ExternalDependencies();
        }

    }

    public void addMissingCodeModifier(String codeModifierClass) {
        missingCodeModifiers.add(codeModifierClass);
    }

    public void setFailedToLoadMixinFlag() {
        failedToLoadMixin = true;
    }

    public List<String> getMissingCodeModifiers() {
        return missingCodeModifiers;
    }

    public boolean hasFailedToLoadMixin() {
        return failedToLoadMixin;
    }

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

    static final class ExternalDependencies {
        Repository[] repositories = new Repository[0];
        String[] artifacts = new String[0];

        static class Repository {
            String name = "";
            String url = "";
        }
    }
}
