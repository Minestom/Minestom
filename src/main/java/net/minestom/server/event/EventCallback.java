package net.minestom.server.event;

import org.jetbrains.annotations.NotNull;

public interface EventCallback<E extends Event> {

    void run(@NotNull E event);

}
