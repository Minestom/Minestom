package net.minestom.server;

import net.minestom.server.acquirable.Acquirable;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.socket.Worker;
import net.minestom.server.thread.MinestomThread;
import net.minestom.server.thread.ThreadDispatcher;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Manager responsible for the server ticks.
 * <p>
 * The {@link ThreadDispatcher} manages the multi-thread aspect of chunk ticks.
 */
public final class UpdateManager {
    private volatile boolean stopRequested;

    // TODO make configurable
    private final ThreadDispatcher threadDispatcher = ThreadDispatcher.singleThread();

    private final Queue<LongConsumer> tickStartCallbacks = new ConcurrentLinkedQueue<>();
    private final Queue<LongConsumer> tickEndCallbacks = new ConcurrentLinkedQueue<>();
    private final List<Consumer<TickMonitor>> tickMonitors = new CopyOnWriteArrayList<>();

    /**
     * Should only be created in MinecraftServer.
     */
    UpdateManager() {
    }

    /**
     * Starts the server loop in the update thread.
     */
    void start() {
        new TickSchedulerThread().start();
    }

    /**
     * Gets the current {@link ThreadDispatcher}.
     *
     * @return the current thread provider
     */
    public @NotNull ThreadDispatcher getThreadProvider() {
        return threadDispatcher;
    }

    /**
     * Signals the {@link ThreadDispatcher} that an instance has been created.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public void signalInstanceCreate(Instance instance) {
        this.threadDispatcher.onInstanceCreate(instance);
    }

    /**
     * Signals the {@link ThreadDispatcher} that an instance has been deleted.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public void signalInstanceDelete(Instance instance) {
        this.threadDispatcher.onInstanceDelete(instance);
    }

    /**
     * Signals the {@link ThreadDispatcher} that a chunk has been loaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param chunk the loaded chunk
     */
    public void signalChunkLoad(@NotNull Chunk chunk) {
        this.threadDispatcher.onChunkLoad(chunk);
    }

    /**
     * Signals the {@link ThreadDispatcher} that a chunk has been unloaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param chunk the unloaded chunk
     */
    public void signalChunkUnload(@NotNull Chunk chunk) {
        this.threadDispatcher.onChunkUnload(chunk);
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

    public void addTickMonitor(@NotNull Consumer<TickMonitor> consumer) {
        this.tickMonitors.add(consumer);
    }

    public void removeTickMonitor(@NotNull Consumer<TickMonitor> consumer) {
        this.tickMonitors.remove(consumer);
    }

    /**
     * Stops the server loop.
     */
    public void stop() {
        this.stopRequested = true;
    }

    private final class TickSchedulerThread extends MinestomThread {
        private final ThreadDispatcher threadDispatcher = UpdateManager.this.threadDispatcher;

        TickSchedulerThread() {
            super(MinecraftServer.THREAD_NAME_TICK_SCHEDULER);
        }

        @Override
        public void run() {
            final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            final List<Worker> workers = MinecraftServer.getServer().workers();
            while (!stopRequested) {
                try {
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

                    // Flush all waiting packets
                    PacketUtils.flush();
                    workers.forEach(Worker::flush);

                    // the time that the tick took in nanoseconds
                    final long tickTime = System.nanoTime() - currentTime;

                    // Tick end callbacks
                    doTickCallback(tickEndCallbacks, tickTime);

                    // Monitoring
                    if (!tickMonitors.isEmpty()) {
                        final double acquisitionTimeMs = Acquirable.getAcquiringTime() / 1e6D;
                        final double tickTimeMs = tickTime / 1e6D;
                        final TickMonitor tickMonitor = new TickMonitor(tickTimeMs, acquisitionTimeMs);
                        for (Consumer<TickMonitor> consumer : tickMonitors) {
                            consumer.accept(tickMonitor);
                        }
                        Acquirable.resetAcquiringTime();
                    }

                    // Disable thread until next tick
                    LockSupport.parkNanos((long) ((MinecraftServer.TICK_MS * 1e6) - tickTime));
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
            this.threadDispatcher.shutdown();
        }

        /**
         * Executes a server tick and returns only once all the futures are completed.
         *
         * @param tickStart the time of the tick in milliseconds
         */
        private void serverTick(long tickStart) {
            // Tick all instances
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                try {
                    instance.tick(tickStart);
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
            // Tick all chunks (and entities inside)
            this.threadDispatcher.updateAndAwait(tickStart);

            // Clear removed entities & update threads
            final long tickTime = System.currentTimeMillis() - tickStart;
            this.threadDispatcher.refreshThreads(tickTime);
        }

        private void doTickCallback(Queue<LongConsumer> callbacks, long value) {
            LongConsumer callback;
            while ((callback = callbacks.poll()) != null) {
                callback.accept(value);
            }
        }
    }
}
