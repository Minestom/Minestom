package net.minestom.server.extensions;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

public abstract class Extension {

    private ExtensionDescription description;
    private Logger logger;

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

    public ExtensionDescription getDescription() {
        return description;
    }

    protected Logger getLogger() {
        return logger;
    }

    protected static class ExtensionDescription {

        private final String name;
        private final String version;
        private final List<String> authors;

        protected ExtensionDescription(@NotNull String name, @NotNull String version, @NotNull List<String> authors) {
            this.name = name;
            this.version = version;
            this.authors = authors;
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
    }
}
