package net.minestom.server.extensions;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public final class HierarchyClassLoader extends URLClassLoader {
    private final List<HierarchyClassLoader> children = new ArrayList<>();

    public HierarchyClassLoader(String name, URL[] urls) {
        super("Ext_" + name, urls, MinecraftServer.class.getClassLoader());
    }

    public HierarchyClassLoader(String name, URL[] urls, ClassLoader parent) {
        super("Ext_" + name, urls, parent);
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }

    public void addChild(@NotNull HierarchyClassLoader loader) {
        children.add(loader);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            for (HierarchyClassLoader child : children) {
                try {
                    return child.loadClass(name, resolve);
                } catch (ClassNotFoundException ignored) {}
            }
            throw e;
        }
    }

    public InputStream getResourceAsStreamWithChildren(@NotNull String name) {
        InputStream in = getResourceAsStream(name);
        if (in != null) return in;

        for (HierarchyClassLoader child : children) {
            InputStream childInput = child.getResourceAsStreamWithChildren(name);
            if (childInput != null)
                return childInput;
        }

        return null;
    }
}
