package net.minestom.server;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.concurrent.ExecutorService;

public final class UpdateManager {

    private static final long KEEP_ALIVE_DELAY = 10_000;
    private static final long KEEP_ALIVE_KICK = 30_000;

    private ExecutorService mainUpdate = new MinestomThread(1, MinecraftServer.THREAD_NAME_MAIN_UPDATE);
    private boolean stopRequested;

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
            final SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();

            final long tickDistance = MinecraftServer.TICK_MS * 1000000;
            long currentTime;
            while (!stopRequested) {
                currentTime = System.nanoTime();

                // Keep Alive Handling
                final long time = System.currentTimeMillis();
                final KeepAlivePacket keepAlivePacket = new KeepAlivePacket(time);
                for (Player player : connectionManager.getOnlinePlayers()) {
                    final long lastKeepAlive = time - player.getLastKeepAlive();
                    if (lastKeepAlive > KEEP_ALIVE_DELAY && player.didAnswerKeepAlive()) {
                        player.refreshKeepAlive(time);
                        player.getPlayerConnection().sendPacket(keepAlivePacket);
                    } else if (lastKeepAlive >= KEEP_ALIVE_KICK) {
                        TextComponent textComponent = TextComponent.of("Timeout")
                                .color(TextColor.RED);
                        player.kick(textComponent);
                    }
                }

                // Entities update
                entityManager.update();

                // Blocks update
                instanceManager.updateBlocks();

                // Scheduler
                schedulerManager.update();

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

    public void stop() {
        stopRequested = true;
    }
}
