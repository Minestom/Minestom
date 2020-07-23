package net.minestom.server.timer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.UpdateOption;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerManager {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final AtomicInteger SHUTDOWN_COUNTER = new AtomicInteger();
    private static ExecutorService batchesPool = new MinestomThread(MinecraftServer.THREAD_COUNT_SCHEDULER, MinecraftServer.THREAD_NAME_SCHEDULER);
    private List<Task> tasks = new CopyOnWriteArrayList<>();
    private List<Task> shutdownTasks = new CopyOnWriteArrayList<>();

    /**
     * Add a task with a custom update option and a precise call count
     *
     * @param runnable     the task to execute
     * @param updateOption the update option of the task
     * @param maxCallCount the number of time this task should be executed
     * @return the task id
     */
    public int addTask(TaskRunnable runnable, UpdateOption updateOption, int maxCallCount) {
        final int id = COUNTER.incrementAndGet();
        runnable.setId(id);

        final Task task = new Task(runnable, updateOption, maxCallCount);
        task.refreshLastUpdateTime(System.currentTimeMillis());
        this.tasks.add(task);

        return id;
    }

    /**
     * Add a task which will be repeated without interruption
     *
     * @param runnable     the task to execute
     * @param updateOption the update option of the task
     * @return the task id
     */
    public int addRepeatingTask(TaskRunnable runnable, UpdateOption updateOption) {
        return addTask(runnable, updateOption, 0);
    }

    /**
     * Add a task which will be executed only once
     *
     * @param runnable     the task to execute
     * @param updateOption the update option of the task
     * @return the task id
     */
    public int addDelayedTask(TaskRunnable runnable, UpdateOption updateOption) {
        return addTask(runnable, updateOption, 1);
    }

    /**
     * Adds a task to run when the server shutdowns
     *
     * @param runnable the task to perform
     * @return the task id
     */
    public int addShutdownTask(TaskRunnable runnable) {
        final int id = SHUTDOWN_COUNTER.incrementAndGet();
        runnable.setId(id);

        final Task task = new Task(runnable, null, 1);
        this.shutdownTasks.add(task);

        return id;
    }

    /**
     * Shutdown all the tasks and call tasks added from {@link #addShutdownTask(TaskRunnable)}
     */
    public void shutdown() {
        batchesPool.execute(() -> {
            for (Task task : shutdownTasks) {
                task.getRunnable().run();
            }
        });
        batchesPool.shutdown();
        try {
            batchesPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Force the end of a task
     *
     * @param taskId the id of the task to remove
     */
    public void removeTask(int taskId) {
        this.tasks.removeIf(task -> task.getId() == taskId);
    }

    public void update() {
        final long time = System.currentTimeMillis();
        batchesPool.execute(() -> {
            for (Task task : tasks) {
                final UpdateOption updateOption = task.getUpdateOption();
                final long lastUpdate = task.getLastUpdateTime();
                final boolean hasCooldown = CooldownUtils.hasCooldown(time, lastUpdate, updateOption.getTimeUnit(), updateOption.getValue());
                if (!hasCooldown) {
                    final TaskRunnable runnable = task.getRunnable();
                    final int maxCallCount = task.getMaxCallCount();
                    final int callCount = runnable.getCallCount() + 1;
                    runnable.setCallCount(callCount);

                    runnable.run();

                    task.refreshLastUpdateTime(time);

                    if (callCount == maxCallCount) {
                        tasks.remove(task);
                    }
                }
            }
        });
    }

}
