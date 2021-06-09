package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

public class EventDispatcher {

    public static void call(@NotNull Event event) {
        MinecraftServer.getGlobalEventHandler().call(event);
    }

    public static void callCancellable(@NotNull CancellableEvent event, @NotNull Runnable successCallback) {
        MinecraftServer.getGlobalEventHandler().callCancellable(event, successCallback);
    }
}
