package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
    private final IntSet registeredTasks = new IntOpenHashSet();
    private final IntSet parkedTasks = new IntOpenHashSet();

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
        final TaskImpl taskRef = register(task, executionType);
        handleTask(taskRef);
        return taskRef;
    }

    synchronized void unparkTask(TaskImpl task) {
        if (parkedTasks.remove(task.id()))
            this.taskQueue.relaxedOffer(task);
    }

    synchronized boolean isTaskParked(TaskImpl task) {
        return parkedTasks.contains(task.id());
    }

    synchronized void cancelTask(TaskImpl task) {
        this.registeredTasks.remove(task.id());
    }

    synchronized boolean isTaskAlive(TaskImpl task) {
        return registeredTasks.contains(task.id());
    }

    private synchronized TaskImpl register(@NotNull Supplier<TaskSchedule> task,
                                           @NotNull ExecutionType executionType) {
        TaskImpl taskRef = new TaskImpl(TASK_COUNTER.getAndIncrement(), task,
                executionType, this);
        this.registeredTasks.add(taskRef.id());
        return taskRef;
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
            synchronized (this) {
                this.parkedTasks.add(task.id());
            }
        } else if (schedule instanceof TaskScheduleImpl.Stop) {
            cancelTask(task);
        } else if (schedule instanceof TaskScheduleImpl.Immediate) {
            this.taskQueue.relaxedOffer(task);
        }
    }
}
