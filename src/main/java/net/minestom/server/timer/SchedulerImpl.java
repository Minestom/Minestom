package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minestom.server.MinecraftServer;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
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

    private final MpscUnboundedArrayQueue<TaskImpl> tasksToExecute = new MpscUnboundedArrayQueue<>(64);
    private final MpscUnboundedArrayQueue<TaskImpl> tickEndTasksToExecute = new MpscUnboundedArrayQueue<>(64);
    // Tasks scheduled on a certain tick/tick end
    private final Int2ObjectAVLTreeMap<List<TaskImpl>> tickStartTaskQueue = new Int2ObjectAVLTreeMap<>();
    private final Int2ObjectAVLTreeMap<List<TaskImpl>> tickEndTaskQueue = new Int2ObjectAVLTreeMap<>();

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
        processTickTasks(tickStartTaskQueue, tasksToExecute, tickDelta);
    }

    @Override
    public void processTickEnd() {
        processTickTasks(tickEndTaskQueue, tickEndTasksToExecute, 0);
    }

    private void processTickTasks(Int2ObjectAVLTreeMap<List<TaskImpl>> targetTaskQueue, MpscUnboundedArrayQueue<TaskImpl> targetTasksToExecute, int tickDelta) {
        synchronized (this) {
            this.tickState += tickDelta;
            int tickToProcess;
            while (!targetTaskQueue.isEmpty() && (tickToProcess = targetTaskQueue.firstIntKey()) <= tickState) {
                final List<TaskImpl> tickScheduledTasks = targetTaskQueue.remove(tickToProcess);
                if (tickScheduledTasks != null) tickScheduledTasks.forEach(targetTasksToExecute::relaxedOffer);
            }
        }
        runTasks(targetTasksToExecute);
    }

    private void runTasks(MpscUnboundedArrayQueue<TaskImpl> targetQueue) {
        // Run all tasks lock-free, either in the current thread or pool
        if (!targetQueue.isEmpty()) {
            targetQueue.drain(task -> {
                if (!task.isAlive()) return;
                handleTask(task);
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
            this.tasksToExecute.relaxedOffer(task);
    }

    private void safeExecute(TaskImpl task) {
        // Prevent the task from being executed in the current thread
        // By either adding the task to the execution queue or submitting it to the pool
        switch (task.executionType()) {
            case TICK_START -> tasksToExecute.offer(task);
            case TICK_END -> tickEndTasksToExecute.offer(task);
        }
    }

    private void handleTask(TaskImpl task) {
        TaskSchedule schedule;
        try {
            schedule = task.task().get();
        } catch (Throwable t) {
            MinecraftServer.getExceptionManager().handleException(new RuntimeException("Exception in scheduled task", t));
            schedule = TaskSchedule.stop();
        }

        if (schedule instanceof TaskScheduleImpl.DurationSchedule durationSchedule) {
            final Duration duration = durationSchedule.duration();
            SCHEDULER.schedule(() -> safeExecute(task), duration.toMillis(), TimeUnit.MILLISECONDS);
        } else if (schedule instanceof TaskScheduleImpl.TickSchedule tickSchedule) {
            synchronized (this) {
                final int target = tickState + tickSchedule.tick();
                var targetTaskQueue = switch (task.executionType()) {
                    case TICK_START -> tickStartTaskQueue;
                    case TICK_END -> tickEndTaskQueue;
                };
                targetTaskQueue.computeIfAbsent(target, i -> new ArrayList<>()).add(task);
            }
        } else if (schedule instanceof TaskScheduleImpl.FutureSchedule futureSchedule) {
            futureSchedule.future().thenRun(() -> safeExecute(task));
        } else if (schedule instanceof TaskScheduleImpl.Park) {
            task.parked = true;
        } else if (schedule instanceof TaskScheduleImpl.Stop) {
            task.cancel();
        } else if (schedule instanceof TaskScheduleImpl.Immediate) {
            if (task.executionType() == ExecutionType.TICK_END) {
                tickEndTasksToExecute.relaxedOffer(task);
            }
            else tasksToExecute.relaxedOffer(task);
        }
    }
}
