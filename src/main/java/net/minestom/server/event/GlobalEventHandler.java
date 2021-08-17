package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Object containing all the global event listeners.
 */
public final class GlobalEventHandler extends EventNodeImpl<Event> {
    public GlobalEventHandler() {
        super("global", EventFilter.ALL, null);
    }

    /**
     * @deprecated use {@link #addListener(Class, Consumer)}
     */
    @Deprecated
    public <V extends Event> boolean addEventCallback(@NotNull Class<V> eventClass, @NotNull EventCallback<V> eventCallback) {
        addListener(eventClass, eventCallback::run);
        return true;
    }
}
