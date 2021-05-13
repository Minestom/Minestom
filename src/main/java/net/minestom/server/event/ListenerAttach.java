package net.minestom.server.event;

import net.minestom.server.event.handler.EventHandler;
import org.jetbrains.annotations.NotNull;

public interface ListenerAttach {
    void attachTo(@NotNull EventHandler handler);

    void detachFrom(@NotNull EventHandler handler);
}
