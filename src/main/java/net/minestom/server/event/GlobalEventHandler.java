package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler implements EventHandler {

    // Events
    private final Map<Class<? extends Event>, Collection<EventCallback>> eventCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Collection<EventCallback<?>>> extensionCallbacks = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Map<Class<? extends Event>, Collection<EventCallback>> getEventCallbacksMap() {
        return eventCallbacks;
    }

    @NotNull
    @Override
    public Collection<EventCallback<?>> getExtensionCallbacks(String extension) {
        return extensionCallbacks.computeIfAbsent(extension, e -> new CopyOnWriteArrayList<>());
    }
}
