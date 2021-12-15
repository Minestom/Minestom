package net.minestom.server.timer;

import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class SchedulerManager implements Scheduler {
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final MpmcUnboundedXaddArrayQueue<Runnable> shutdownTasks = new MpmcUnboundedXaddArrayQueue<>(1024);

    @Override
    public void process() {
        this.scheduler.process();
    }

    @Override
    public void processTick() {
        this.scheduler.processTick();
    }

    @Override
    public @NotNull Task submitTask(@NotNull Supplier<TaskSchedule> task,
                                    @NotNull ExecutionType executionType) {
        return scheduler.submitTask(task, executionType);
    }

    public void shutdown() {
        this.shutdownTasks.drain(Runnable::run);
    }

    public void buildShutdownTask(@NotNull Runnable runnable) {
        this.shutdownTasks.relaxedOffer(runnable);
    }
}
