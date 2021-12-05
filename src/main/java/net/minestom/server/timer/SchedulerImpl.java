package net.minestom.server.timer;

import com.zaxxer.sparsebits.SparseBitSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minestom.server.MinecraftServer;
import org.jctools.queues.MpscGrowableArrayQueue;
import org.jetbrains.annotations.NotNull;

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

    private final Set<Task> tasks = ConcurrentHashMap.newKeySet();
    private final SparseBitSet bitSet = new SparseBitSet();

    private final Int2ObjectAVLTreeMap<List<Task>> tickTaskQueue = new Int2ObjectAVLTreeMap<>();
    private final MpscGrowableArrayQueue<Task> taskQueue = new MpscGrowableArrayQueue<>(64);
    private final Set<Task> parkedTasks = ConcurrentHashMap.newKeySet();

    private final AtomicInteger tickState = new AtomicInteger();

    @Override
    public void process() {
        processTick(tickState.get());
    }

    @Override
    public void processTick() {
        processTick(tickState.incrementAndGet());
    }

    private void processTick(int tick) {
        synchronized (this) {
            int tickToProcess;
            while ((tickToProcess = !tickTaskQueue.isEmpty() ? tickTaskQueue.firstIntKey() : Integer.MAX_VALUE) <= tick) {
                final List<Task> tickScheduledTasks = tickTaskQueue.remove(tickToProcess);
                if (tickScheduledTasks != null) tickScheduledTasks.forEach(taskQueue::relaxedOffer);
            }
        }
        // Run all tasks lock-free
        this.taskQueue.drain(this::execute);
    }

    @Override
    public @NotNull Task submit(@NotNull Supplier<TaskSchedule> task,
                                @NotNull ExecutionType executionType) {
        final TaskImpl taskRef = register(task, executionType);
        execute(taskRef);
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

    void unparkTask(Task task) {
        if (!parkedTasks.remove(task)) {
            throw new IllegalStateException("Task is not parked");
        }
        execute(task);
        // TODO task executed on next processing?
        // this.taskQueue.relaxedOffer(task);
    }

    synchronized void stopTask(Task task) {
        this.bitSet.clear(task.id());
        if (!tasks.remove(task)) throw new IllegalStateException("Task is not scheduled");
    }

    synchronized boolean isTaskAlive(TaskImpl task) {
        return bitSet.get(task.id());
    }

    private synchronized TaskImpl register(@NotNull Supplier<TaskSchedule> task,
                                           @NotNull ExecutionType executionType) {
        TaskImpl taskRef = new TaskImpl(TASK_COUNTER.getAndIncrement(), task,
                executionType, this);
        this.bitSet.set(taskRef.id());
        this.tasks.add(taskRef);
        return taskRef;
    }

    private void execute(Task task) {
        if (!task.isAlive()) return;
        switch (task.executionType()) {
            case SYNC -> handleStatus(task, ((TaskImpl) task).task().get());
            case ASYNC -> EXECUTOR.submit(() -> handleStatus(task, ((TaskImpl) task).task().get()));
        }
    }

    private void handleStatus(Task task, TaskSchedule schedule) {
        if (schedule instanceof TaskScheduleImpl.DurationSchedule durationSchedule) {
            final Duration duration = durationSchedule.duration();
            SCHEDULER.schedule(() -> {
                switch (task.executionType()) {
                    case SYNC -> taskQueue.offer(task);
                    case ASYNC -> execute(task);
                }
            }, duration.toMillis(), TimeUnit.MILLISECONDS);
        } else if (schedule instanceof TaskScheduleImpl.TickSchedule tickSchedule) {
            final int target = tickState.get() + tickSchedule.tick();
            synchronized (this) {
                this.tickTaskQueue.computeIfAbsent(target, i -> new ArrayList<>()).add(task);
            }
        } else if (schedule instanceof TaskScheduleImpl.FutureSchedule futureSchedule) {
            futureSchedule.future().whenComplete((o, throwable) -> {
                if (throwable != null) {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                    return;
                }
                execute(task);
            });
        } else if (schedule instanceof TaskScheduleImpl.Park) {
            this.parkedTasks.add(task);
        } else if (schedule instanceof TaskScheduleImpl.Stop) {
            this.tasks.remove(task);
        } else if (schedule instanceof TaskScheduleImpl.Immediate) {
            this.taskQueue.relaxedOffer(task);
        }
    }
}
