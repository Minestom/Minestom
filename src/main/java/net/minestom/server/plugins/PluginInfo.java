package net.minestom.server.plugins;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PluginInfo {

    private @Nullable Plugin plugin;
    private @Nullable PluginDescription description;

    private final File file;
    private PluginState state = PluginState.INITIALIZING;

    public PluginInfo(File file) {
        this.file = file;
    }

    public void setPlugin(@Nullable Plugin plugin) {
        this.plugin = plugin;
    }

    public void setState(PluginState state) {
        this.state = state;
    }

    public @Nullable PluginDescription getDescription() {
        return description;
    }

    public void setDescription(@Nullable PluginDescription description) {
        this.description = description;
    }

    public @Nullable Plugin getPlugin() {
        return plugin;
    }

    public File getFile() {
        return file;
    }

    public PluginState getState() {
        return state;
    }

}
