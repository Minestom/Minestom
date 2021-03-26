package net.minestom.server.extras.selfmodification;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * Classloader part of a hierarchy of classloader
 */
public abstract class HierarchyClassLoader extends URLClassLoader {
    protected final List<MinestomExtensionClassLoader> children = new LinkedList<>();

    public HierarchyClassLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, parent);
    }

    public void addChild(@NotNull MinestomExtensionClassLoader loader) {
        children.add(loader);
    }

    public InputStream getResourceAsStreamWithChildren(@NotNull String name) {
        InputStream in = getResourceAsStream(name);
        if (in != null) return in;

        for (MinestomExtensionClassLoader child : children) {
            InputStream childInput = child.getResourceAsStreamWithChildren(name);
            if (childInput != null)
                return childInput;
        }

        return null;
    }

    public void removeChildInHierarchy(MinestomExtensionClassLoader child) {
        children.remove(child);

        // Also remove all children from these extension's children.
        for (MinestomExtensionClassLoader subChild : children) {
            subChild.removeChildInHierarchy(child);
        }
    }
}
