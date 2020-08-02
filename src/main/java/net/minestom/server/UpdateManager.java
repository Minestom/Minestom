package net.minestom.server;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.thread.PerInstanceThreadProvider;
import net.minestom.server.thread.ThreadProvider;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;

import java.util.concurrent.ExecutorService;

public final class UpdateManager {

    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;

    private ExecutorService mainUpdate = new MinestomThread(1, MinecraftServer.THREAD_NAME_MAIN_UPDATE);
    private boolean stopRequested;

    private ThreadProvider threadProvider;

    {
        threadProvider = new PerInstanceThreadProvider();
        //threadProvider = new PerGroupChunkProvider();
    }

    /**
     * Should only be created in MinecraftServer
     */
    protected UpdateManager() {
    }

    public void start() {
        mainUpdate.execute(() -> {

            final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            final EntityManager entityManager = MinecraftServer.getEntityManager();
            final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

            final long tickDistance = MinecraftServer.TICK_MS * 1000000;
            long currentTime;
            while (!stopRequested) {
                currentTime = System.nanoTime();

                // Server tick
                //long testTime = System.nanoTime();
                threadProvider.start();
                for (Instance instance : instanceManager.getInstances()) {
                    for (Chunk chunk : instance.getChunks()) {
                        threadProvider.linkThread(instance, chunk);
                    }
                }
                threadProvider.end();
                threadProvider.update();
                //System.out.println("time: " + (System.nanoTime() - testTime));

                // Waiting players update
                entityManager.updateWaitingPlayers();

                // Keep Alive Handling
                final long time = System.currentTimeMillis();
                final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(time);
                for (Player player : connectionManager.getOnlinePlayers()) {
                    final long lastKeepAlive = time - player.getLastKeepAlive();
                    if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                        player.refreshKeepAlive(time);
                        player.getPlayerConnection().sendPacket(keepAlivePacket);
                    } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                        player.kick(ColoredText.of(ChatColor.RED + "Timeout"));
                    }
                }

                // Sleep until next tick
                long sleepTime = (tickDistance - (System.nanoTime() - currentTime)) / 1000000;
                sleepTime = Math.max(1, sleepTime);

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * Get the current thread provider
     *
     * @return the current thread provider
     */
    public ThreadProvider getThreadProvider() {
        return threadProvider;
    }

    /**
     * Change the server thread provider
     *
     * @param threadProvider the new thread provider
     * @throws NullPointerException if {@param threadProvider} is null
     */
    public void setThreadProvider(ThreadProvider threadProvider) {
        Check.notNull(threadProvider, "The thread provider cannot be null");
        this.threadProvider = threadProvider;
    }

    public void stop() {
        stopRequested = true;
    }
}
