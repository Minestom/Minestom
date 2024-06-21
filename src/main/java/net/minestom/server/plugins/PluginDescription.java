package net.minestom.server.plugins;

public final class PluginDescription {

    private final String name;
    private final String version;
    private final String author;
    private final String entrypoint;
    private final String[] dependencies;

    public PluginDescription(String name, String version, String author, String entrypoint, String[] dependencies) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.entrypoint = entrypoint;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public String[] getDependencies() {
        return dependencies;
    }
}
