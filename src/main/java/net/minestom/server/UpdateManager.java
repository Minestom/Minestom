package net.minestom.server;

import net.minestom.server.entity.EntityManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.thread.PerInstanceThreadProvider;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.LongConsumer;

/**
 * Manager responsible for the server ticks.
 * <p>
 * The {@link ThreadProvider} manages the multi-thread aspect for {@link Instance} ticks,
 * it can be modified with {@link #setThreadProvider(ThreadProvider)}.
 */
public final class UpdateManager {

    private final ExecutorService mainUpdate = new MinestomThread(1, MinecraftServer.THREAD_NAME_MAIN_UPDATE);
    private boolean stopRequested;

    private ThreadProvider threadProvider;

    private final ConcurrentLinkedQueue<LongConsumer> tickStartCallbacks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<LongConsumer> tickEndCallbacks = new ConcurrentLinkedQueue<>();

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
        mainUpdate.execute(() -> {
            final EntityManager entityManager = MinecraftServer.getEntityManager();

            final long tickDistance = MinecraftServer.TICK_MS * 1000000;
            long currentTime;
            while (!stopRequested) {
                currentTime = System.nanoTime();
                final long tickStart = System.currentTimeMillis();

                // Tick start callbacks
                doTickCallback(tickStartCallbacks, tickStart);

                // Waiting players update (newly connected clients waiting to get into the server)
                entityManager.updateWaitingPlayers();

                // Keep Alive Handling
                entityManager.handleKeepAlive(tickStart);

                // Server tick (chunks/entities)
                serverTick(tickStart);

                // the time that the tick took in nanoseconds
                final long tickTime = System.nanoTime() - currentTime;

                // Tick end callbacks
                doTickCallback(tickEndCallbacks, tickTime / 1000000L);

                // Sleep until next tick
                final long sleepTime = Math.max(1, (tickDistance - tickTime) / 1000000L);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    if (!stopRequested)
                        e.printStackTrace();
                }
            }

        });
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
                e.printStackTrace();
            }
        }
    }

    /**
     * Used to execute tick-related callbacks.
     *
     * @param callbacks the callbacks to execute
     * @param value     the value to give to the consumers
     */
    private void doTickCallback(ConcurrentLinkedQueue<LongConsumer> callbacks, long value) {
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
        Check.notNull(threadProvider, "The thread provider cannot be null");
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
