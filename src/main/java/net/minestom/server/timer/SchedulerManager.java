package net.minestom.server.timer;

import net.minestom.server.ServerFlag;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

import java.util.function.Supplier;

public final class SchedulerManager implements Scheduler {
    private final Scheduler scheduler = Scheduler.newScheduler();
    private final MessagePassingQueue<Runnable> shutdownTasks = ServerFlag.UNSAFE_COLLECTIONS ? new MpmcUnboundedXaddArrayQueue<>(1024) : new MpscUnboundedAtomicArrayQueue<>(1024); // Single consumer in atomic mode.

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
