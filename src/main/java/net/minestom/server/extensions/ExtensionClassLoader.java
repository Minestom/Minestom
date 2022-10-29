package net.minestom.server.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public final class ExtensionClassLoader extends URLClassLoader {
    private final List<ExtensionClassLoader> children = new ArrayList<>();
    private final DiscoveredExtension discoveredExtension;
    private EventNode<Event> eventNode;
    private ComponentLogger logger;

    public ExtensionClassLoader(String name, URL[] urls, DiscoveredExtension discoveredExtension) {
        super("Ext_" + name, urls, MinecraftServer.class.getClassLoader());
        this.discoveredExtension = discoveredExtension;
    }

    public ExtensionClassLoader(String name, URL[] urls, ClassLoader parent, DiscoveredExtension discoveredExtension) {
        super("Ext_" + name, urls, parent);
        this.discoveredExtension = discoveredExtension;
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }

    public void addChild(@NotNull ExtensionClassLoader loader) {
        children.add(loader);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            for (ExtensionClassLoader child : children) {
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

        for (ExtensionClassLoader child : children) {
            InputStream childInput = child.getResourceAsStreamWithChildren(name);
            if (childInput != null)
                return childInput;
        }

        return null;
    }

    public DiscoveredExtension getDiscoveredExtension() {
        return discoveredExtension;
    }

    public EventNode<Event> getEventNode() {
        if (eventNode == null) {
            eventNode = EventNode.all(discoveredExtension.getName());
            MinecraftServer.getGlobalEventHandler().addChild(eventNode);
        }
        return eventNode;
    }

    public ComponentLogger getLogger() {
        if (logger == null) {
            logger = ComponentLogger.logger(discoveredExtension.getName());
        }
        return logger;
    }

    void terminate() {
        if (eventNode != null) {
            MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
        }
    }
}
