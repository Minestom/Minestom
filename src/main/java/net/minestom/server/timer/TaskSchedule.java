package net.minestom.server.timer;


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
    static TaskSchedule duration(Duration duration) {
        return new TaskScheduleImpl.DurationSchedule(duration);
    }

    static TaskSchedule tick(int tick) {
        return new TaskScheduleImpl.TickSchedule(tick);
    }

    static TaskSchedule future(CompletableFuture<?> future) {
        return new TaskScheduleImpl.FutureSchedule(future);
    }

    static TaskSchedule park() {
        return TaskScheduleImpl.PARK;
    }

    static TaskSchedule stop() {
        return TaskScheduleImpl.STOP;
    }

    static TaskSchedule immediate() {
        return TaskScheduleImpl.IMMEDIATE;
    }

    // Shortcuts

    static TaskSchedule duration(long amount, TemporalUnit unit) {
        return duration(Duration.of(amount, unit));
    }

    static TaskSchedule nextTick() {
        return TaskScheduleImpl.NEXT_TICK;
    }

    static TaskSchedule hours(long hours) {
        return duration(Duration.ofHours(hours));
    }

    static TaskSchedule minutes(long minutes) {
        return duration(Duration.ofMinutes(minutes));
    }

    static TaskSchedule seconds(long seconds) {
        return duration(Duration.ofSeconds(seconds));
    }

    static TaskSchedule millis(long millis) {
        return duration(Duration.ofMillis(millis));
    }
}
