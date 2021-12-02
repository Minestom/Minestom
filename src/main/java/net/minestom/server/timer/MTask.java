package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public sealed interface MTask permits MTaskImpl {
    int id();

    @NotNull ExecutionType executionType();

    @NotNull Scheduler owner();

    void wakeup();

    void stop();

    sealed interface Status permits
            MTaskImpl.DurationStatus,
            MTaskImpl.FutureStatus,
            MTaskImpl.ParkStatus,
            MTaskImpl.StopStatus,
            MTaskImpl.TickStatus {
        static @NotNull MTask.Status scheduleDuration(@NotNull Duration duration) {
            return new MTaskImpl.DurationStatus(duration);
        }

        static @NotNull MTask.Status scheduleTick(int tick) {
            return new MTaskImpl.TickStatus(tick);
        }

        static @NotNull MTask.Status scheduleFuture(@NotNull CompletableFuture<?> future) {
            return new MTaskImpl.FutureStatus(future);
        }

        static @NotNull MTask.Status park() {
            return new MTaskImpl.ParkStatus();
        }

        static @NotNull MTask.Status stop() {
            return new MTaskImpl.StopStatus();
        }
    }

    enum ExecutionType {
        SYNC,
        ASYNC
    }
}
