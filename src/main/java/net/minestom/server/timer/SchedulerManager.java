package net.minestom.server.timer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.IExtensionObserver;
import net.minestom.server.utils.thread.MinestomThread;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * An object which manages all the {@link Task}'s.
 * <p>
 * {@link Task} first need to be built with {@link #buildTask(Runnable)}, you can then specify a delay with as example
 * {@link TaskBuilder#delay(long, net.minestom.server.utils.time.TimeUnit)}
 * or {@link TaskBuilder#repeat(long, net.minestom.server.utils.time.TimeUnit)},
 * and to finally schedule: {@link TaskBuilder#schedule()}.
 * <p>
 * Shutdown tasks are built with {@link #buildShutdownTask(Runnable)} and are executed, as the name implies, when the server stops.
 */
public final class SchedulerManager implements IExtensionObserver {

    private static boolean instanced;
    // A counter for all normal tasks
    private final AtomicInteger counter;
    // A counter for all shutdown tasks
    private final AtomicInteger shutdownCounter;
    //A threaded execution
    private final ExecutorService batchesPool;
    // A single threaded scheduled execution
    private final ScheduledExecutorService timerExecutionService;
    // All the registered tasks (task id = task)
    protected final Int2ObjectMap<Task> tasks;
    // All the registered shutdown tasks (task id = task)
    protected final Int2ObjectMap<Task> shutdownTasks;

    /**
     * Tasks scheduled through extensions
     */
    private final Map<String, List<Task>> extensionTasks = new ConcurrentHashMap<>();

    /**
     * Shutdown tasks scheduled through extensions
     */
    private final Map<String, List<Task>> extensionShutdownTasks = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public SchedulerManager() {
        if (instanced) {
            throw new IllegalStateException("You cannot instantiate a SchedulerManager," +
                    " use MinecraftServer.getSchedulerManager()");
        }
        SchedulerManager.instanced = true;

        this.counter = new AtomicInteger();
        this.shutdownCounter = new AtomicInteger();

        this.batchesPool = new MinestomThread(MinecraftServer.THREAD_COUNT_SCHEDULER, MinecraftServer.THREAD_NAME_SCHEDULER);
        this.timerExecutionService = Executors.newSingleThreadScheduledExecutor();
        this.tasks = new Int2ObjectOpenHashMap<>();
        this.shutdownTasks = new Int2ObjectOpenHashMap<>();
    }

    /**
     * Initializes a new {@link TaskBuilder} for creating a {@link Task}.
     *
     * @param runnable The {@link Task} to run when scheduled
     * @return the {@link TaskBuilder}
     */
    @NotNull
    public TaskBuilder buildTask(@NotNull Runnable runnable) {
        return new TaskBuilder(this, runnable);
    }

    /**
     * Initializes a new {@link TaskBuilder} for creating a shutdown {@link Task}.
     *
     * @param runnable The shutdown {@link Task} to run when scheduled
     * @return the {@link TaskBuilder}
     */
    @NotNull
    public TaskBuilder buildShutdownTask(@NotNull Runnable runnable) {
        return new TaskBuilder(this, runnable, true);
    }

    /**
     * Removes/Forces the end of a {@link Task}.
     * <p>
     * {@link Task#cancel()} can also be used instead.
     *
     * @param task The {@link Task} to remove
     */
    public void removeTask(@NotNull Task task) {
        task.cancel();
    }

    /**
     * Shutdowns all normal tasks and call the registered shutdown tasks.
     */
    public void shutdown() {
        MinecraftServer.LOGGER.info("Executing all shutdown tasks..");
        for (Task task : this.getShutdownTasks()) {
            task.runRunnable();
        }
        MinecraftServer.LOGGER.info("Shutting down the scheduled execution service and batches pool.");
        this.timerExecutionService.shutdown();
        this.batchesPool.shutdown();
        try {
            batchesPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    /**
     * Increments the current counter value.
     *
     * @return the updated counter value
     */
    protected int getCounterIdentifier() {
        return this.counter.incrementAndGet();
    }

    /**
     * Increments the current shutdown counter value.
     *
     * @return the updated shutdown counter value
     */
    protected int getShutdownCounterIdentifier() {
        return this.shutdownCounter.incrementAndGet();
    }

    /**
     * Gets a {@link Collection} with all the registered {@link Task}.
     * <p>
     * Be aware that the collection is not thread-safe.
     *
     * @return a {@link Collection} with all the registered {@link Task}
     */
    @NotNull
    public ObjectCollection<Task> getTasks() {
        return tasks.values();
    }

    /**
     * Gets a {@link Collection} with all the registered shutdown {@link Task}.
     *
     * @return a {@link Collection} with all the registered shutdown {@link Task}
     */
    @NotNull
    public ObjectCollection<Task> getShutdownTasks() {
        return shutdownTasks.values();
    }

    /**
     * Gets the execution service for all the registered {@link Task}.
     *
     * @return the execution service for all the registered {@link Task}
     */
    @NotNull
    public ExecutorService getBatchesPool() {
        return batchesPool;
    }

    /**
     * Gets the scheduled execution service for all the registered {@link Task}.
     *
     * @return the scheduled execution service for all the registered {@link Task}
     */
    @NotNull
    public ScheduledExecutorService getTimerExecutionService() {
        return timerExecutionService;
    }

    /**
     * Called when a Task from an extension is scheduled.
     * @param owningExtension the name of the extension which scheduled the task
     * @param task the task that has been scheduled
     */
    void onScheduleFromExtension(String owningExtension, Task task) {
        List<Task> scheduledForThisExtension = extensionTasks.computeIfAbsent(owningExtension, s -> new CopyOnWriteArrayList<>());
        scheduledForThisExtension.add(task);

        Extension ext = MinecraftServer.getExtensionManager().getExtension(owningExtension);
        ext.observe(this);
    }

    /**
     * Called when a Task from an extension is scheduled for server shutdown.
     * @param owningExtension the name of the extension which scheduled the task
     * @param task the task that has been scheduled
     */
    void onScheduleShutdownFromExtension(String owningExtension, Task task) {
        List<Task> scheduledForThisExtension = extensionShutdownTasks.computeIfAbsent(owningExtension, s -> new CopyOnWriteArrayList<>());
        scheduledForThisExtension.add(task);

        Extension ext = MinecraftServer.getExtensionManager().getExtension(owningExtension);
        ext.observe(this);
    }

    /**
     * Unschedules all non-transient tasks ({@link Task#isTransient()}) from this scheduler. Tasks are allowed to complete
     * @param extension the name of the extension to unschedule tasks from
     * @see Task#isTransient()
     */
    public void removeExtensionTasksOnUnload(String extension) {
        List<Task> scheduledForThisExtension = extensionTasks.get(extension);
        if(scheduledForThisExtension != null) {
            List<Task> toCancel = scheduledForThisExtension.stream()
                    .filter(t -> !t.isTransient())
                    .collect(Collectors.toList());
            toCancel.forEach(Task::cancel);
            scheduledForThisExtension.removeAll(toCancel);
        }


        List<Task> shutdownScheduledForThisExtension = extensionShutdownTasks.get(extension);
        if(shutdownScheduledForThisExtension != null) {
            List<Task> toCancel = shutdownScheduledForThisExtension.stream()
                    .filter(t -> !t.isTransient())
                    .collect(Collectors.toList());
            toCancel.forEach(Task::cancel);
            shutdownScheduledForThisExtension.removeAll(toCancel);
            shutdownTasks.values().removeAll(toCancel);
        }
    }

    @Override
    public void onExtensionUnload(String extensionName) {
        removeExtensionTasksOnUnload(extensionName);
    }
}
