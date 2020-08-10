package net.minestom.server.plugins;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class PluginLoader {

	private static PluginLoader instance = null;

	//Singleton
	public static PluginLoader getInstance() {
		if (instance == null) {
			instance = new PluginLoader();
		}
		return instance;
	}

	private PluginLoader() {

	}

	public List<Plugin> loadPlugin(String path) {
		JarFile jarFile;
		URLClassLoader cl;
		try {
			jarFile = new JarFile(path);
			URL[] urls = new URL[]{new URL("jar:file:" + path + "!/")};
			cl = URLClassLoader.newInstance(urls);
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
		final List<Plugin> plugins = new ArrayList<>();
		final Enumeration<JarEntry> e = jarFile.entries();
		while (e.hasMoreElements()) {
			try {
				final JarEntry je = e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) continue;
				// -6 because of .class
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				final Class<?> c;
				c = cl.loadClass(className);
				Type superclass = c.getGenericSuperclass();
				if (superclass != null && Plugin.class.getTypeName().equals(superclass.getTypeName()))
					try {
						plugins.add((Plugin) c.getConstructor().newInstance());
					} catch (final ReflectiveOperationException | ArrayIndexOutOfBoundsException ex) {
						ex.printStackTrace();
					}
			} catch (final Throwable ex) {
				ex.printStackTrace();
			}
		}

		return Collections.unmodifiableList(plugins);
	}

}
