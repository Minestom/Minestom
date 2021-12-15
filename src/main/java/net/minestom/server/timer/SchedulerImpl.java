package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import org.jctools.queues.MpscGrowableArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.roaringbitmap.RoaringBitmap;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
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

    private final Set<TaskImpl> tasks = ConcurrentHashMap.newKeySet();
    private final RoaringBitmap bitSet = new RoaringBitmap();

    private final Int2ObjectAVLTreeMap<List<TaskImpl>> tickTaskQueue = new Int2ObjectAVLTreeMap<>();
    private final MpscGrowableArrayQueue<TaskImpl> taskQueue = new MpscGrowableArrayQueue<>(64);
    private final Set<TaskImpl> parkedTasks = ConcurrentHashMap.newKeySet();

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
        this.taskQueue.drain(task -> {
            if (!task.isAlive()) return;
            switch (task.executionType()) {
                case SYNC -> handleStatus(task, task.task().get());
                case ASYNC -> EXECUTOR.submit(() -> handleStatus(task, task.task().get()));
            }
        });
    }

    @Override
    public @NotNull Task submit(@NotNull Supplier<TaskSchedule> task,
                                @NotNull ExecutionType executionType) {
        final TaskImpl taskRef = register(task, executionType);
        safeExecute(taskRef);
        return taskRef;
    }

    @Override
    public @NotNull Task submitAfter(@NotNull TaskSchedule schedule,
                                     @NotNull Supplier<TaskSchedule> task,
                                     @NotNull ExecutionType executionType) {
        final TaskImpl taskRef = register(task, executionType);
        handleStatus(taskRef, schedule);
        return taskRef;
    }

    @Override
    public @NotNull Collection<@NotNull Task> scheduledTasks() {
        return Collections.unmodifiableSet(tasks);
    }

    void unparkTask(TaskImpl task) {
        if (parkedTasks.remove(task)) {
            this.taskQueue.relaxedOffer(task);
        }
    }

    boolean isTaskParked(TaskImpl task) {
        return parkedTasks.contains(task);
    }

    synchronized void cancelTask(TaskImpl task) {
        this.bitSet.remove(task.id());
        if (!tasks.remove(task)) throw new IllegalStateException("Task is not scheduled");
    }

    synchronized boolean isTaskAlive(TaskImpl task) {
        return bitSet.contains(task.id());
    }

    private synchronized TaskImpl register(@NotNull Supplier<TaskSchedule> task,
                                           @NotNull ExecutionType executionType) {
        TaskImpl taskRef = new TaskImpl(TASK_COUNTER.getAndIncrement(), task,
                executionType, this);
        this.bitSet.add(taskRef.id());
        this.tasks.add(taskRef);
        return taskRef;
    }

    private void safeExecute(TaskImpl task) {
        // Prevent the task from being executed in the current thread
        // By either adding the task to the execution queue or submitting it to the pool
        switch (task.executionType()) {
            case SYNC -> taskQueue.offer(task);
            case ASYNC -> EXECUTOR.submit(() -> handleStatus(task, task.task().get()));
        }
    }

    private void handleStatus(TaskImpl task, TaskSchedule schedule) {
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
            this.parkedTasks.add(task);
        } else if (schedule instanceof TaskScheduleImpl.Stop) {
            this.tasks.remove(task);
        } else if (schedule instanceof TaskScheduleImpl.Immediate) {
            this.taskQueue.relaxedOffer(task);
        }
    }
}
