package net.minestom.server.timer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

public sealed interface Scheduler permits SchedulerImpl {
    /**
     * Process scheduled tasks based on time.
     * <p>
     * Can be used to increase scheduling precision.
     */
    void process();

    /**
     * Advance 1 tick and call {@link #process()}.
     */
    void processTick();

    @NotNull MTask submit(@NotNull Supplier<MTask.Status> task,
                          @NotNull MTask.ExecutionType executionType);

    @NotNull Collection<@NotNull MTask> scheduledTasks();

    static @NotNull Scheduler newScheduler() {
        return new SchedulerImpl();
    }
}
