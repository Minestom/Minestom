package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minestom.server.MinecraftServer;
import org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

final class SchedulerImpl implements Scheduler {
    private static final AtomicInteger TASK_COUNTER = new AtomicInteger();
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ForkJoinPool EXECUTOR = ForkJoinPool.commonPool();

    private final List<MTask> tasks = new CopyOnWriteArrayList<>();
    private final Int2ObjectAVLTreeMap<List<MTask>> tickTaskQueue = new Int2ObjectAVLTreeMap<>();
    private final MpmcUnboundedXaddArrayQueue<MTask> taskQueue = new MpmcUnboundedXaddArrayQueue<>(1024);
    private final Set<MTask> parkedTasks = ConcurrentHashMap.newKeySet();

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
        // Tasks based on tick
        synchronized (tickTaskQueue) {
            int tickToProcess;
            while ((tickToProcess = tickTaskQueue.firstIntKey()) <= tick) {
                final List<MTask> tickScheduledTasks = tickTaskQueue.remove(tickToProcess);
                if (tickScheduledTasks != null) tickScheduledTasks.forEach(this::execute);
            }
        }
        // Tasks based on time
        this.taskQueue.drain(this::execute);
    }

    @Override
    public @NotNull MTask submit(@NotNull Supplier<MTask.Status> task,
                                 @NotNull MTask.ExecutionType executionType) {
        MTaskImpl taskRef = new MTaskImpl(TASK_COUNTER.getAndIncrement(), task,
                executionType, this);
        this.tasks.add(taskRef);
        execute(taskRef);
        return taskRef;
    }

    @Override
    public @NotNull List<@NotNull MTask> scheduledTasks() {
        return Collections.unmodifiableList(tasks);
    }

    void wakeupTask(MTask task) {
        if (parkedTasks.remove(task)) {
            execute(task);
        }
    }

    void stopTask(MTask mTask) {
        // TODO: stop task
    }

    private void execute(MTask task) {
        switch (task.executionType()) {
            case SYNC -> handleStatus(task);
            case ASYNC -> EXECUTOR.submit(() -> handleStatus(task));
        }
    }

    private void handleStatus(MTask task) {
        final MTask.Status status = ((MTaskImpl) task).task().get();
        if (status instanceof MTaskImpl.DurationStatus durationStatus) {
            final Duration duration = durationStatus.duration();
            SCHEDULER.schedule(() -> taskQueue.offer(task), duration.toMillis(), TimeUnit.MILLISECONDS);
        } else if (status instanceof MTaskImpl.TickStatus tickStatus) {
            final int target = tickState.get() + tickStatus.tick();
            synchronized (tickTaskQueue) {
                this.tickTaskQueue.computeIfAbsent(target, i -> new ArrayList<>()).add(task);
            }
        } else if (status instanceof MTaskImpl.FutureStatus futureStatus) {
            futureStatus.future().whenComplete((o, throwable) -> {
                if (throwable != null) {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                    return;
                }
                execute(task);
            });
        } else if (status instanceof MTaskImpl.ParkStatus) {
            this.parkedTasks.add(task);
        } else if (status instanceof MTaskImpl.StopStatus) {
            this.tasks.remove(task);
        }
    }
}
