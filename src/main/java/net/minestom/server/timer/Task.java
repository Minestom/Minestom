package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

public sealed interface Task permits TaskImpl {
    int id();

    @NotNull ExecutionType executionType();

    @NotNull Scheduler owner();

    /**
     * Unpark the tasks to be executed during next processing.
     */
    void unpark();

    boolean isParked();

    void cancel();

    boolean isAlive();

    final class Builder {
        private final Scheduler scheduler;
        private final Runnable runnable;
        private ExecutionType executionType = ExecutionType.SYNC;
        private TaskSchedule delay = TaskSchedule.immediate();
        private TaskSchedule repeat = TaskSchedule.stop();

        Builder(Scheduler scheduler, Runnable runnable) {
            this.scheduler = scheduler;
            this.runnable = runnable;
        }

        public @NotNull Builder executionType(@NotNull ExecutionType executionType) {
            this.executionType = executionType;
            return this;
        }

        public @NotNull Builder delay(@NotNull TaskSchedule schedule) {
            this.delay = schedule;
            return this;
        }

        public @NotNull Builder repeat(@NotNull TaskSchedule schedule) {
            this.repeat = schedule;
            return this;
        }

        public @NotNull Task schedule() {
            var runnable = this.runnable;
            var delay = this.delay;
            var repeat = this.repeat;
            var executionType = this.executionType;
            return scheduler.submitTask(new Supplier<>() {
                boolean first = true;

                @Override
                public TaskSchedule get() {
                    if (first) {
                        first = false;
                        return delay;
                    }
                    runnable.run();
                    return repeat;
                }
            }, executionType);
        }

        public @NotNull Builder delay(@NotNull Duration duration) {
            return delay(TaskSchedule.duration(duration));
        }

        public @NotNull Builder delay(long time, @NotNull TemporalUnit unit) {
            return delay(Duration.of(time, unit));
        }

        public @NotNull Builder repeat(@NotNull Duration duration) {
            return repeat(TaskSchedule.duration(duration));
        }

        public @NotNull Builder repeat(long time, @NotNull TemporalUnit unit) {
            return repeat(Duration.of(time, unit));
        }
    }

}
