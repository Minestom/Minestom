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
        private final Supplier<TaskSchedule> innerTask;
        private ExecutionType executionType = ExecutionType.TICK_START;
        private TaskSchedule delay = TaskSchedule.immediate();
        private TaskSchedule repeat = TaskSchedule.stop();
        private boolean repeatOverride;

        Builder(Scheduler scheduler, Supplier<TaskSchedule> innerTask) {
            this.scheduler = scheduler;
            this.innerTask = innerTask;
        }

        Builder(Scheduler scheduler, Runnable runnable) {
            this.scheduler = scheduler;
            this.innerTask = () -> {
                runnable.run();
                return TaskSchedule.stop();
            };
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
            this.repeatOverride = true;
            return this;
        }

        public @NotNull Task schedule() {
            var innerTask = this.innerTask;
            var delay = this.delay;
            var repeat = this.repeat;
            var repeatOverride = this.repeatOverride;
            var executionType = this.executionType;
            return scheduler.submitTask(new Supplier<>() {
                boolean first = true;

                @Override
                public TaskSchedule get() {
                    if (first) {
                        first = false;
                        return delay;
                    }
                    TaskSchedule schedule = innerTask.get();
                    if (repeatOverride) {
                        return repeat;
                    }
                    return schedule;
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
