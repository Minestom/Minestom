package net.minestom.server;

import net.minestom.server.acquirable.Acquirable;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.thread.SingleThreadProvider;
import net.minestom.server.thread.ThreadProvider;
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
 * The {@link ThreadProvider} manages the multi-thread aspect of chunk ticks.
 */
public final class UpdateManager {

    private volatile boolean stopRequested;

    // TODO make configurable
    private ThreadProvider threadProvider = new SingleThreadProvider();

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
        final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        new Thread(() -> {
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

                    // the time that the tick took in nanoseconds
                    final long tickTime = System.nanoTime() - currentTime;

                    // Tick end callbacks
                    doTickCallback(tickEndCallbacks, tickTime);

                    // Monitoring
                    if (!tickMonitors.isEmpty()) {
                        final double acquisitionTimeMs = Acquirable.getAcquiringTime() / 1e6D;
                        final double tickTimeMs = tickTime / 1e6D;
                        final TickMonitor tickMonitor = new TickMonitor(tickTimeMs, acquisitionTimeMs);
                        this.tickMonitors.forEach(consumer -> consumer.accept(tickMonitor));
                        Acquirable.resetAcquiringTime();
                    }

                    // Flush all waiting packets
                    for (Player player : connectionManager.getOnlinePlayers()) {
                        player.getPlayerConnection().flush();
                    }

                    // Disable thread until next tick
                    LockSupport.parkNanos((long) ((MinecraftServer.TICK_MS * 1e6) - tickTime));
                } catch (Exception e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
            }
            this.threadProvider.shutdown();
        }, MinecraftServer.THREAD_NAME_TICK_SCHEDULER).start();
    }

    /**
     * Executes a server tick and returns only once all the futures are completed.
     *
     * @param tickStart the time of the tick in milliseconds
     */
    private void serverTick(long tickStart) {
        // Tick all instances
        MinecraftServer.getInstanceManager().getInstances().forEach(instance -> {
            try {
                instance.tick(tickStart);
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
        // Tick all chunks (and entities inside)
        this.threadProvider.updateAndAwait(tickStart);

        // Clear removed entities & update threads
        long tickTime = System.currentTimeMillis() - tickStart;
        this.threadProvider.refreshThreads(tickTime);
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
    public @NotNull ThreadProvider getThreadProvider() {
        return threadProvider;
    }

    /**
     * Signals the {@link ThreadProvider} that an instance has been created.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public void signalInstanceCreate(Instance instance) {
        this.threadProvider.onInstanceCreate(instance);
    }

    /**
     * Signals the {@link ThreadProvider} that an instance has been deleted.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public void signalInstanceDelete(Instance instance) {
        this.threadProvider.onInstanceDelete(instance);
    }

    /**
     * Signals the {@link ThreadProvider} that a chunk has been loaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param chunk the loaded chunk
     */
    public void signalChunkLoad(@NotNull Chunk chunk) {
        this.threadProvider.onChunkLoad(chunk);
    }

    /**
     * Signals the {@link ThreadProvider} that a chunk has been unloaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param chunk the unloaded chunk
     */
    public void signalChunkUnload(@NotNull Chunk chunk) {
        this.threadProvider.onChunkUnload(chunk);
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
}
