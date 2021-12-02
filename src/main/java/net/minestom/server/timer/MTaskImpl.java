package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

record MTaskImpl(int id,
                 @NotNull Supplier<Status> task,
                 @NotNull ExecutionType executionType,
                 @NotNull SchedulerImpl owner) implements MTask {
    public void wakeup() {
        this.owner.wakeupTask(this);
    }

    record DurationStatus(@NotNull Duration duration) implements MTask.Status {
    }

    record TickStatus(int tick) implements MTask.Status {
        public TickStatus {
            if (tick <= 0)
                throw new IllegalArgumentException("Tick must be greater than 0 (" + tick + ")");
        }
    }

    record FutureStatus(CompletableFuture<?> future) implements MTask.Status {
    }

    record ParkStatus() implements MTask.Status {
    }

    record StopStatus() implements MTask.Status {
    }
}
