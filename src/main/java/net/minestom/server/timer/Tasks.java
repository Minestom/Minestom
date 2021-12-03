package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class Tasks {
    private Tasks() {
    }

    public static @NotNull Supplier<TaskSchedule> nextTick(@NotNull Runnable runnable) {
        return () -> {
            runnable.run();
            return TaskSchedule.stop();
        };
    }
}
