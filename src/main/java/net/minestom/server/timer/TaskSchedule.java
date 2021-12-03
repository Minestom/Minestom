package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public sealed interface TaskSchedule permits
        TaskScheduleImpl.DurationSchedule,
        TaskScheduleImpl.FutureSchedule,
        TaskScheduleImpl.Park,
        TaskScheduleImpl.Stop,
        TaskScheduleImpl.TickSchedule {
    static @NotNull TaskSchedule duration(@NotNull Duration duration) {
        return new TaskScheduleImpl.DurationSchedule(duration);
    }

    static @NotNull TaskSchedule tick(int tick) {
        return new TaskScheduleImpl.TickSchedule(tick);
    }

    static @NotNull TaskSchedule future(@NotNull CompletableFuture<?> future) {
        return new TaskScheduleImpl.FutureSchedule(future);
    }

    static @NotNull TaskSchedule park() {
        return TaskScheduleImpl.PARK;
    }

    static @NotNull TaskSchedule stop() {
        return TaskScheduleImpl.STOP;
    }
}
