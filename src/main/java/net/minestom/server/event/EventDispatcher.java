package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.MutableEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class EventDispatcher {

    public static void call(@NotNull Event event) {
        MinecraftServer.getGlobalEventHandler().call(event);
    }

    public static <E extends Event> ListenerHandle<E> getHandle(@NotNull Class<E> handleType) {
        return MinecraftServer.getGlobalEventHandler().getHandle(handleType);
    }

    // TODO remove this?
    public static <T extends CancellableEvent<T>> T callCancellable(@NotNull T event) {
        return callMutable(event);
    }

    // All events are considered mutable when they are cancelable.
    public static <T extends CancellableEvent<T>> T callCancellable(@NotNull T event, @NotNull Consumer<T> consumer) {
        return MinecraftServer.getGlobalEventHandler().callCancellable(event, consumer);
    }

    public static <T extends CancellableEvent<T>> T callCancellable(@NotNull T event, @NotNull Runnable successCallback) {
        return MinecraftServer.getGlobalEventHandler().callCancellable(event, successCallback);
    }

    public static <E extends MutableEvent<E>> E callMutable(@NotNull E event) {
       return MinecraftServer.getGlobalEventHandler().callMutable(event);
    }

}
