package net.minestom.server;

import com.google.common.collect.Queues;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.thread.PerInstanceThreadProvider;
import net.minestom.server.thread.ThreadProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

/**
 * Manager responsible for the server ticks.
 * <p>
 * The {@link ThreadProvider} manages the multi-thread aspect for {@link Instance} ticks,
 * it can be modified with {@link #setThreadProvider(ThreadProvider)}.
 */
public final class UpdateManager {

    private final ScheduledExecutorService updateExecutionService = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, "tick-scheduler"));

    private volatile boolean stopRequested;

    private ThreadProvider threadProvider;

    private final Queue<LongConsumer> tickStartCallbacks = Queues.newConcurrentLinkedQueue();
    private final Queue<LongConsumer> tickEndCallbacks = Queues.newConcurrentLinkedQueue();

    {
        // DEFAULT THREAD PROVIDER
        //threadProvider = new PerGroupChunkProvider();
        threadProvider = new PerInstanceThreadProvider();
    }

    /**
     * Should only be created in MinecraftServer.
     */
    protected UpdateManager() {
    }

    /**
     * Starts the server loop in the update thread.
     */
    protected void start() {
        final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        updateExecutionService.scheduleAtFixedRate(() -> {
            try {
                if (stopRequested) {
                    updateExecutionService.shutdown();
                    return;
                }

                long currentTime = System.nanoTime();
                final long tickStart = System.currentTimeMillis();

                // Tick start callbacks
                doTickCallback(tickStartCallbacks, tickStart);

                // Waiting players update (newly connected clients waiting to get into the server)
                connectionManager.updateWaitingPlayers();

                // Keep Alive Handling
                connectionManager.handleKeepAlive(tickStart);

                // Server tick (chunks/entities)
                serverTick(tickStart);

                // the time that the tick took in nanoseconds
                final long tickTime = System.nanoTime() - currentTime;

                // Tick end callbacks
                doTickCallback(tickEndCallbacks, tickTime / 1000000L);

            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }, 0, MinecraftServer.TICK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Executes a server tick and returns only once all the futures are completed.
     *
     * @param tickStart the time of the tick in milliseconds
     */
    private void serverTick(long tickStart) {
        List<Future<?>> futures;

        // Server tick (instance/chunk/entity)
        // Synchronize with the update manager instance, like the signal for chunk load/unload
        synchronized (this) {
            futures = threadProvider.update(tickStart);
        }

        for (final Future<?> future : futures) {
            try {
                future.get();
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }
    }

    /**
     * Used to execute tick-related callbacks.
     *
     * @param callbacks the callbacks to execute
     * @param value     the value to give to the consumers
     */
    private void doTickCallback(Queue<LongConsumer> callbacks, long value) {
        if (!callbacks.isEmpty()) {
            LongConsumer callback;
            while ((callback = callbacks.poll()) != null) {
                callback.accept(value);
            }
        }
    }

    /**
     * Gets the current {@link ThreadProvider}.
     *
     * @return the current thread provider
     */
    public ThreadProvider getThreadProvider() {
        return threadProvider;
    }

    /**
     * Changes the server {@link ThreadProvider}.
     *
     * @param threadProvider the new thread provider
     * @throws NullPointerException if <code>threadProvider</code> is null
     */
    public synchronized void setThreadProvider(ThreadProvider threadProvider) {
        this.threadProvider = threadProvider;
    }

    /**
     * Signals the {@link ThreadProvider} that an instance has been created.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public synchronized void signalInstanceCreate(Instance instance) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onInstanceCreate(instance);
    }

    /**
     * Signals the {@link ThreadProvider} that an instance has been deleted.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public synchronized void signalInstanceDelete(Instance instance) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onInstanceDelete(instance);
    }

    /**
     * Signals the {@link ThreadProvider} that a chunk has been loaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public synchronized void signalChunkLoad(Instance instance, int chunkX, int chunkZ) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onChunkLoad(instance, chunkX, chunkZ);
    }

    /**
     * Signals the {@link ThreadProvider} that a chunk has been unloaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public synchronized void signalChunkUnload(Instance instance, int chunkX, int chunkZ) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onChunkUnload(instance, chunkX, chunkZ);
    }

    /**
     * Adds a callback executed at the start of the next server tick.
     * <p>
     * The long in the consumer represents the starting time (in ms) of the tick.
     *
     * @param callback the tick start callback
     */
    public void addTickStartCallback(@NotNull LongConsumer callback) {
        this.tickStartCallbacks.add(callback);
    }

    /**
     * Removes a tick start callback.
     *
     * @param callback the callback to remove
     */
    public void removeTickStartCallback(@NotNull LongConsumer callback) {
        this.tickStartCallbacks.remove(callback);
    }

    /**
     * Adds a callback executed at the end of the next server tick.
     * <p>
     * The long in the consumer represents the duration (in ms) of the tick.
     *
     * @param callback the tick end callback
     */
    public void addTickEndCallback(@NotNull LongConsumer callback) {
        this.tickEndCallbacks.add(callback);
    }

    /**
     * Removes a tick end callback.
     *
     * @param callback the callback to remove
     */
    public void removeTickEndCallback(@NotNull LongConsumer callback) {
        this.tickEndCallbacks.remove(callback);
    }

    /**
     * Stops the server loop.
     */
    public void stop() {
        stopRequested = true;
    }
}
