package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.CompletableFuture;

public sealed interface TaskSchedule permits
        TaskScheduleImpl.DurationSchedule,
        TaskScheduleImpl.FutureSchedule,
        TaskScheduleImpl.Immediate,
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

    static @NotNull TaskSchedule immediate() {
        return TaskScheduleImpl.IMMEDIATE;
    }

    // Shortcuts

    static @NotNull TaskSchedule duration(long amount, @NotNull TemporalUnit unit) {
        return duration(Duration.of(amount, unit));
    }

    static @NotNull TaskSchedule nextTick() {
        return TaskScheduleImpl.NEXT_TICK;
    }

    static @NotNull TaskSchedule hours(long hours) {
        return duration(Duration.ofHours(hours));
    }

    static @NotNull TaskSchedule minutes(long minutes) {
        return duration(Duration.ofMinutes(minutes));
    }

    static @NotNull TaskSchedule seconds(long seconds) {
        return duration(Duration.ofSeconds(seconds));
    }

    static @NotNull TaskSchedule millis(long millis) {
        return duration(Duration.ofMillis(millis));
    }
}
