package net.minestom.server.item.event;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventCallback;
import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemEventHandler implements EventHandler {

    private final Map<Class<? extends Event>, Collection<EventCallback>> eventCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Collection<EventCallback<?>>> extensionCallbacks = new ConcurrentHashMap<>();

    ItemEventHandler(String namespace) {

    }

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
