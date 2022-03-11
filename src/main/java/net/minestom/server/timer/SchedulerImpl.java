package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

final class SchedulerImpl implements Scheduler {
    private static final AtomicInteger TASK_COUNTER = new AtomicInteger();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    });
    private static final ForkJoinPool EXECUTOR = ForkJoinPool.commonPool();

    private final MpscUnboundedArrayQueue<TaskImpl> taskQueue = new MpscUnboundedArrayQueue<>(64);
    // Tasks scheduled on a certain tick
    private final Int2ObjectAVLTreeMap<List<TaskImpl>> tickTaskQueue = new Int2ObjectAVLTreeMap<>();

    private int tickState;

    @Override
    public void process() {
        processTick(0);
    }

    @Override
    public void processTick() {
        processTick(1);
    }

    private void processTick(int tickDelta) {
        synchronized (this) {
            this.tickState += tickDelta;
            int tickToProcess;
            while (!tickTaskQueue.isEmpty() && (tickToProcess = tickTaskQueue.firstIntKey()) <= tickState) {
                final List<TaskImpl> tickScheduledTasks = tickTaskQueue.remove(tickToProcess);
                if (tickScheduledTasks != null) tickScheduledTasks.forEach(taskQueue::relaxedOffer);
            }
        }
        // Run all tasks lock-free, either in the current thread or pool
        if (!taskQueue.isEmpty()) {
            this.taskQueue.drain(task -> {
                if (!task.isAlive()) return;
                switch (task.executionType()) {
                    case SYNC -> handleTask(task);
                    case ASYNC -> EXECUTOR.submit(() -> handleTask(task));
                }
            });
        }
    }

    @Override
    public @NotNull Task submitTask(@NotNull Supplier<TaskSchedule> task,
                                    @NotNull ExecutionType executionType) {
        final TaskImpl taskRef = new TaskImpl(TASK_COUNTER.getAndIncrement(), task,
                executionType, this);
        handleTask(taskRef);
        return taskRef;
    }

    void unparkTask(TaskImpl task) {
        if (task.tryUnpark())
            this.taskQueue.relaxedOffer(task);
    }

    private void safeExecute(TaskImpl task) {
        // Prevent the task from being executed in the current thread
        // By either adding the task to the execution queue or submitting it to the pool
        switch (task.executionType()) {
            case SYNC -> taskQueue.offer(task);
            case ASYNC -> EXECUTOR.submit(() -> handleTask(task));
        }
    }

    private void handleTask(TaskImpl task) {
        final TaskSchedule schedule = task.task().get();
        if (schedule instanceof TaskScheduleImpl.DurationSchedule durationSchedule) {
            final Duration duration = durationSchedule.duration();
            SCHEDULER.schedule(() -> safeExecute(task), duration.toMillis(), TimeUnit.MILLISECONDS);
        } else if (schedule instanceof TaskScheduleImpl.TickSchedule tickSchedule) {
            synchronized (this) {
                final int target = tickState + tickSchedule.tick();
                this.tickTaskQueue.computeIfAbsent(target, i -> new ArrayList<>()).add(task);
            }
        } else if (schedule instanceof TaskScheduleImpl.FutureSchedule futureSchedule) {
            futureSchedule.future().thenRun(() -> safeExecute(task));
        } else if (schedule instanceof TaskScheduleImpl.Park) {
            task.parked = true;
        } else if (schedule instanceof TaskScheduleImpl.Stop) {
            task.cancel();
        } else if (schedule instanceof TaskScheduleImpl.Immediate) {
            this.taskQueue.relaxedOffer(task);
        }
    }
}
