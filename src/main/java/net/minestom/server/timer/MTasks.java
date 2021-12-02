package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class MTasks {
    private MTasks() {
    }

    public static @NotNull Supplier<MTask.Status> nextTick(@NotNull Runnable runnable) {
        return () -> {
            runnable.run();
            return MTask.Status.stop();
        };
    }
}
