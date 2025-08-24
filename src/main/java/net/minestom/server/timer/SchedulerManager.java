package net.minestom.server.timer;

import org.jctools.queues.MpmcUnboundedXaddArrayQueue;

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
    public void processTickEnd() {
        this.scheduler.processTickEnd();
    }

    @Override
    public Task submitTask(Supplier<TaskSchedule> task,
                                    ExecutionType executionType) {
        return scheduler.submitTask(task, executionType);
    }

    public void shutdown() {
        this.shutdownTasks.drain(Runnable::run);
    }

    public void buildShutdownTask(Runnable runnable) {
        this.shutdownTasks.relaxedOffer(runnable);
    }
}
