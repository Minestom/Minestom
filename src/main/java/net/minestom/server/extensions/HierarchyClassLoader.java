package net.minestom.server.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HierarchyClassLoader extends URLClassLoader {
    private final List<HierarchyClassLoader> children = new CopyOnWriteArrayList<>();

    public HierarchyClassLoader(String name, URL[] urls, ClassLoader parent) {
        super("HCL_" + name, urls, parent);
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
                } catch (ClassNotFoundException ignored) {
                }
            }
            throw e;
        }
    }

    @Nullable
    @Override
    public URL getResource(String name) {
        URL resource = super.getResource(name);
        if (resource != null) {
            return resource;
        }

        for (HierarchyClassLoader child : children) {
            if ((resource = child.getResource(name)) != null) {
                return resource;
            }
        }

        return null;
    }
}
