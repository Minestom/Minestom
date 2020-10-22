package net.minestom.server;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.thread.PerGroupChunkProvider;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.DoubleConsumer;

/**
 * Manager responsible for the server ticks.
 * <p>
 * The {@link ThreadProvider} manages the multi-thread aspect for {@link Instance} ticks,
 * it can be modified with {@link #setThreadProvider(ThreadProvider)}.
 */
public final class UpdateManager {

    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;
    private static final ColoredText TIMEOUT_TEXT = ColoredText.of(ChatColor.RED + "Timeout");

    private final ExecutorService mainUpdate = new MinestomThread(1, MinecraftServer.THREAD_NAME_MAIN_UPDATE);
    private boolean stopRequested;

    private ThreadProvider threadProvider;

    private final ConcurrentLinkedQueue<Runnable> tickStartCallbacks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<DoubleConsumer> tickEndCallbacks = new ConcurrentLinkedQueue<>();

    {
        //threadProvider = new PerInstanceThreadProvider();
        threadProvider = new PerGroupChunkProvider();
    }

    /**
     * Should only be created in MinecraftServer
     */
    protected UpdateManager() {
    }

    /**
     * Starts the server loop in the update thread
     */
    protected void start() {
        mainUpdate.execute(() -> {

            final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            final EntityManager entityManager = MinecraftServer.getEntityManager();

            final long tickDistance = MinecraftServer.TICK_MS * 1000000;
            long currentTime;
            while (!stopRequested) {
                currentTime = System.nanoTime();
                final long tickStart = System.currentTimeMillis();

                // Tick start callbacks
                if (!tickStartCallbacks.isEmpty()) {
                    Runnable callback;
                    while ((callback = tickStartCallbacks.poll()) != null) {
                        callback.run();
                    }
                }

                List<Future<?>> futures;

                // Server tick (instance/chunk/entity)
                // Synchronize with the update manager instance, like the signal for chunk load/unload
                synchronized (this) {
                    futures = threadProvider.update(tickStart);
                }

                // Waiting players update (newly connected clients waiting to get into the server)
                entityManager.updateWaitingPlayers();

                // Keep Alive Handling
                final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(tickStart);
                for (Player player : connectionManager.getOnlinePlayers()) {
                    final long lastKeepAlive = tickStart - player.getLastKeepAlive();
                    if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                        player.refreshKeepAlive(tickStart);
                        player.getPlayerConnection().sendPacket(keepAlivePacket);
                    } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                        player.kick(TIMEOUT_TEXT);
                    }
                }

                for (final Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }


                // Tick end callbacks
                if (!tickEndCallbacks.isEmpty()) {
                    final double tickEnd = (System.nanoTime() - currentTime) / 1000000D;
                    DoubleConsumer callback;
                    while ((callback = tickEndCallbacks.poll()) != null) {
                        callback.accept(tickEnd);
                    }
                }

                // Sleep until next tick
                final long sleepTime = Math.max(1, (tickDistance - (System.nanoTime() - currentTime)) / 1000000);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
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
     *
     * @param callback the tick start callback
     */
    public void addTickStartCallback(Runnable callback) {
        this.tickStartCallbacks.add(callback);
    }

    /**
     * Removes a tick start callback.
     *
     * @param callback the callback to remove
     */
    public void removeTickStartCallback(Runnable callback) {
        this.tickStartCallbacks.remove(callback);
    }

    /**
     * Adds a callback executed at the end of the next server tick.
     * <p>
     * The double in the consumer represents the duration (in ms) of the tick.
     *
     * @param callback the tick end callback
     */
    public void addTickEndCallback(DoubleConsumer callback) {
        this.tickEndCallbacks.add(callback);
    }

    /**
     * Removes a tick end callback.
     *
     * @param callback the callback to remove
     */
    public void removeTickEndCallback(DoubleConsumer callback) {
        this.tickEndCallbacks.remove(callback);
    }

    /**
     * Stops the server loop
     */
    public void stop() {
        stopRequested = true;
    }
}
