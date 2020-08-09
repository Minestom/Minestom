package net.minestom.server.timer;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An object which manages all the {@link Task}'s
 */
public class SchedulerManager {

    private boolean instanced;
    // A counter for all normal tasks
    private final AtomicInteger counter;
    // A counter for all shutdown tasks
    private final AtomicInteger shutdownCounter;
    //A threaded execution
    private final ExecutorService batchesPool;
    // A single threaded scheduled execution
    private final ScheduledExecutorService timerExecutionService;
    // A list with all normal registered tasks
    private final List<Task> tasks;
    // A list with all registered shutdown tasks
    private final List<Task> shutdownTasks;

    /**
     * Default constructor
     */
    public SchedulerManager() {
        if (instanced) {
            throw new IllegalStateException("You cannot instantiate a SchedulerManager," +
                    " use MinecraftServer.getSchedulerManager()");
        }
        this.instanced = true;
        this.counter = new AtomicInteger();
        this.shutdownCounter = new AtomicInteger();

        this.batchesPool = new MinestomThread(MinecraftServer.THREAD_COUNT_SCHEDULER, MinecraftServer.THREAD_NAME_SCHEDULER);
        this.timerExecutionService = Executors.newSingleThreadScheduledExecutor();
        this.tasks = new CopyOnWriteArrayList<>();
        this.shutdownTasks = new CopyOnWriteArrayList<>();
    }

    /**
     * Initializes a new {@link TaskBuilder} for creating a task.
     *
     * @param runnable The task to run when scheduled
     * @return the task builder
     */
    public TaskBuilder buildTask(Runnable runnable) {
        return new TaskBuilder(this, runnable);
    }

    /**
     * Initializes a new {@link TaskBuilder} for creating a shutdown task
     *
     * @param runnable The shutdown task to run when scheduled
     * @return the task builder
     */
    public TaskBuilder buildShutdownTask(Runnable runnable) {
        return new TaskBuilder(this, runnable, true);
    }

    /**
     * Removes/Forces the end of a task
     *
     * @param task The task to remove
     */
    public void removeTask(Task task) {
        this.tasks.removeIf(toRemove -> toRemove.equals(task));
    }

    /**
     * Removes/Forces the end of a task
     *
     * @param task The task to remove
     */
    public void removeShutdownTask(Task task) {
        this.tasks.removeIf(toRemove -> toRemove.equals(task));
    }

    /**
     * Shutdowns all normal tasks and call the registered shutdown tasks
     */
    public void shutdown() {
        MinecraftServer.getLOGGER().info("Executing all shutdown tasks..");
        for (Task task : this.getShutdownTasks()) {
            task.schedule();
        }
        MinecraftServer.getLOGGER().info("Shutting down the scheduled execution service and batches pool.");
        this.timerExecutionService.shutdown();
        this.batchesPool.shutdown();
        try {
            batchesPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Increments the current counter value.
     *
     * @return the updated counter value
     */
    public int getCounterIdentifier() {
        return this.counter.incrementAndGet();
    }

    /**
     * Increments the current shutdown counter value
     *
     * @return the updated shutdown counter value
     */
    public int getShutdownCounterIdentifier() {
        return this.shutdownCounter.incrementAndGet();
    }

    /**
     * Gets a {@link List} with all registered tasks
     *
     * @return a {@link List} with all registered tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Gets a {@link List} with all registered shutdown tasks
     *
     * @return a {@link List} with all registered shutdown tasks
     */
    public List<Task> getShutdownTasks() {
        return shutdownTasks;
    }

    /**
     * Gets the execution service for all registered tasks
     *
     * @return the execution service for all registered tasks
     */
    public ExecutorService getBatchesPool() {
        return batchesPool;
    }

    /**
     * Gets the scheduled execution service for all registered tasks
     *
     * @return the scheduled execution service for all registered tasks
     */
    public ScheduledExecutorService getTimerExecutionService() {
        return timerExecutionService;
    }
}
