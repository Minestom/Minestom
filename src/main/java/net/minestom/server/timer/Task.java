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
     *
     * @throws IllegalStateException if the task is not parked
     */
    void unpark();

    void stop();

    boolean isAlive();

    final class Builder {
        private final Scheduler scheduler;
        private final Runnable runnable;
        private ExecutionType executionType = ExecutionType.SYNC;
        private TaskSchedule startSchedule;
        private TaskSchedule stepSchedule;

        Builder(Scheduler scheduler, Runnable runnable) {
            this.scheduler = scheduler;
            this.runnable = runnable;
        }

        public @NotNull Builder executionType(@NotNull ExecutionType executionType) {
            this.executionType = executionType;
            return this;
        }

        public @NotNull Builder startSchedule(@NotNull TaskSchedule schedule) {
            this.startSchedule = schedule;
            return this;
        }

        public @NotNull Builder stepSchedule(@NotNull TaskSchedule schedule) {
            this.stepSchedule = schedule;
            return this;
        }

        public @NotNull Builder delay(@NotNull Duration duration) {
            return startSchedule(TaskSchedule.duration(duration));
        }

        public @NotNull Builder delay(long time, @NotNull TemporalUnit unit) {
            return delay(Duration.of(time, unit));
        }

        public @NotNull Builder repeat(@NotNull Duration duration) {
            return stepSchedule(TaskSchedule.duration(duration));
        }

        public @NotNull Builder repeat(long time, @NotNull TemporalUnit unit) {
            return repeat(Duration.of(time, unit));
        }

        public @NotNull Task schedule() {
            final Supplier<TaskSchedule> supplier = () -> {
                runnable.run();
                return stepSchedule != null ? stepSchedule : TaskSchedule.stop();
            };
            return startSchedule != null ? scheduler.submitAfter(startSchedule, supplier, executionType) : scheduler.submit(supplier, executionType);
        }
    }

}
