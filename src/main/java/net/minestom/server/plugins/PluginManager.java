package net.minestom.server.plugins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PluginManager {

    private final PluginClassLoader pluginClassLoader = new PluginClassLoader(this.getClass().getClassLoader());

    private static final Gson PLUGIN_GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    private final List<PluginInfo> plugins = new ArrayList<>();
    private boolean loaded = false;

    public PluginManager() {

    }

    public void loadPlugins() {
        Check.stateCondition(loaded, "Plugins have already been loaded!");

        var pluginDirectory = Path.of("plugins");

        try {
            if (!Files.exists(pluginDirectory)) {
                Files.createDirectory(pluginDirectory);
            }

            Files.list(pluginDirectory).filter(path -> path.toString().endsWith(".jar")).forEach(path -> plugins.add(new PluginInfo(path.toFile())));

            // load all meta data
            for (var plugin : plugins) {
                var jarUrl = new URL("jar:file:" + plugin.getFile().getAbsolutePath() + "!/plugin.json");

                try (var inputStream = jarUrl.openStream()) {
                    var description = PLUGIN_GSON.fromJson(new BufferedReader(new InputStreamReader(inputStream)), PluginDescription.class);
                    plugin.setDescription(description);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MinecraftServer.LOGGER.info("Loaded {} plugins: {}", plugins.size(), String.join(", ", plugins.stream()
                .filter(it -> it.getDescription() != null)
                .map(plugin -> plugin.getDescription().getName())
                .toArray(String[]::new)));


        for (var plugin : plugins.stream().sorted(Comparator.comparing(it -> it.getDescription().getDependencies().length)).toList()) {
            if (plugin.getState() != PluginState.RUNNING && plugin.getState() != PluginState.LOADED) {
                this.loadPlugin(plugin);
            }
        }

        loaded = true;
    }

    private void loadPlugin(PluginInfo plugin) {

        if (plugin.getDescription() == null) {
            return;
        }

        try {

            if (plugin.getDescription().getDependencies().length > 1) {
                for (var depend : plugin.getDescription().getDependencies()) {
                    var pluginInfo = getPlugin(depend);
                    if (pluginInfo == null) {
                        MinecraftServer.LOGGER.error("Plugin {} depends on {} but it's not loaded", plugin.getDescription().getName(), depend);
                        return;
                    }
                    if (pluginInfo.getState() != PluginState.RUNNING && pluginInfo.getState() != PluginState.LOADED) {
                        this.loadPlugin(pluginInfo);
                    }
                }
            }

            pluginClassLoader.addURL(plugin.getFile().toURI().toURL());

            Plugin instance = (Plugin) Class.forName(plugin.getDescription().getEntrypoint(), true, pluginClassLoader).getConstructor().newInstance();

            plugin.setPlugin(instance);
            plugin.setState(PluginState.LOADED);
            instance.onEnable();
            plugin.setState(PluginState.RUNNING);
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException | InvocationTargetException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public List<PluginInfo> getPlugins() {
        return plugins;
    }

    public @Nullable PluginInfo getPlugin(String name) {
        return plugins.stream().filter(plugin -> plugin.getDescription() != null && plugin.getDescription().getName().equals(name)).findFirst().orElse(null);
    }

    public void unloadPlugin(PluginInfo plugin) {
        //todo: unload plugin
    }
}
