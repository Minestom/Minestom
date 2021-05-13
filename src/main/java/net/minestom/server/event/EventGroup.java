package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

public class EventGroup implements ListenerAttach {

    private final EventListener<?>[] listeners;

    protected EventGroup(@NotNull EventListener<?>... listeners) {
        this.listeners = listeners;
    }

    @Override
    public void attachTo(@NotNull EventHandler handler) {
        for (EventListener<?> listener : listeners) {
            listener.attachTo(handler);
        }
    }

    @Override
    public void detachFrom(@NotNull EventHandler handler) {
        for (EventListener<?> listener : listeners) {
            listener.detachFrom(handler);
        }
    }
}
