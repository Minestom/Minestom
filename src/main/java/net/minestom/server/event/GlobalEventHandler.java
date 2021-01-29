package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler implements EventHandler {

    // Events
    private final Map<Class<? extends Extension>, Map<Class<? extends Event>, Collection<EventCallback>>> eventCallbacks = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public <V extends Extension> Map<Class<? extends Event>, Collection<EventCallback>> getEventCallbacksMap(Class<V> extensionClass) {
        eventCallbacks.putIfAbsent(extensionClass, new ConcurrentHashMap<>());
        return eventCallbacks.get(extensionClass);
    }

    @Override
    public <V extends Extension> void clearExtension(Class<V> extensionClass) {
        eventCallbacks.remove(extensionClass);
    }
}
