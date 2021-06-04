package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class EventDispatcher {

    public static void call(@NotNull Event event) {
        MinecraftServer.getGlobalEventNode().call(event);
    }

    public static void callCancellable(@NotNull CancellableEvent event, @NotNull Runnable successCallback) {
        MinecraftServer.getGlobalEventNode().callCancellable(event, successCallback);
    }
}
