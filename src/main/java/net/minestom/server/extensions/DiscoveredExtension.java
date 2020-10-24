package net.minestom.server.extensions;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@Slf4j
class DiscoveredExtension {
    private static String NAME_REGEX = "[A-Za-z][_A-Za-z0-9]+";

    enum LoadStatus {
        LOAD_SUCCESS("Actually, it did not fail. This message should not have been printed."),
        MISSING_DEPENDENCIES("Missing dependencies, check your logs."),
        INVALID_NAME("Invalid name."),
        NO_ENTRYPOINT("No entrypoint specified."),
        ;

        private final String message;

        LoadStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    static class Dependencies {
        static class Repository {
            String name;
            String url;
        }

        Repository[] repositories;
        String[] artifacts;
    }

    transient File[] files = new File[0];
    transient LoadStatus loadStatus = LoadStatus.LOAD_SUCCESS;

    private String[] codeModifiers;
    private String[] authors;
    private String mixinConfig;
    private String name;
    private String version;
    private String entrypoint;
    private Dependencies dependencies;

    void checkIntegrity() {
        if(name == null) {
            StringBuilder fileList = new StringBuilder();
            for(File f : files) {
                fileList.append(f.getAbsolutePath()).append(", ");
            }
            log.error("Extension with no name. (at {}})", fileList);
            log.error("Extension at ({}) will not be loaded.", fileList);
            loadStatus = LoadStatus.INVALID_NAME;
            return;
        }
        if(!name.matches(NAME_REGEX)) {
            log.error("Extension '{}' specified an invalid name.", name);
            log.error("Extension '{}' will not be loaded.", name);
            loadStatus = LoadStatus.INVALID_NAME;
            return;
        }
        if(entrypoint == null) {
            log.error("Extension '{}' did not specify an entry point (via 'entrypoint').", name);
            log.error("Extension '{}' will not be loaded.", name);
            loadStatus = LoadStatus.NO_ENTRYPOINT;
            return;
        }
        if(codeModifiers == null) {
            codeModifiers = new String[0];
        }
    }

    @NotNull
    public String getName() {
        if(name == null) {
            throw new IllegalStateException("Missing extension name");
        }
        return name;
    }

    @NotNull
    public String[] getCodeModifiers() {
        if(codeModifiers == null) {
            codeModifiers = new String[0];
        }
        return codeModifiers;
    }

    @Nullable
    public String getMixinConfig() {
        return mixinConfig;
    }

    @Nullable
    public String[] getAuthors() {
        return authors;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    @NotNull
    public String getEntrypoint() {
        return entrypoint;
    }

    @Nullable
    public Dependencies getDependencies() {
        return dependencies;
    }
}
