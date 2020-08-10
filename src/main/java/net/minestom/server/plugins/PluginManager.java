package net.minestom.server.plugins;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class PluginManager {

	private static PluginManager instance = null;

	//Singleton
	public static PluginManager getInstance() {
		if (instance == null) {
			instance = new PluginManager();
		}
		return instance;
	}

	private final PluginLoader loader = PluginLoader.getInstance();

	private final File pluginsDir;

	private PluginManager() {
		pluginsDir = new File("plugins");
		if (!pluginsDir.exists()||!pluginsDir.isDirectory()) {
			if (!pluginsDir.mkdir()) {
				log.error("Couldn't create plugins dir, plugins will not be loaded.");
				return;
			}
		}
	}

	public void loadPlugins() {

		File[] files = pluginsDir.listFiles();
		if(files != null) {
			for (final File plugin : files) {
				loader.loadPlugin(plugin.getPath());
			}
		}
	}
}
