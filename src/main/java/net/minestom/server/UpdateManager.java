package net.minestom.server;

import net.minestom.server.entity.EntityManager;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.KeepAlivePacket;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.concurrent.ExecutorService;

public class UpdateManager {

    private ExecutorService mainUpdate = new MinestomThread(MinecraftServer.THREAD_COUNT_MAIN_UPDATE, MinecraftServer.THREAD_NAME_MAIN_UPDATE);


    public void start() {
        mainUpdate.execute(() -> {

            ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
            EntityManager entityManager = MinecraftServer.getEntityManager();
            InstanceManager instanceManager = MinecraftServer.getInstanceManager();
            SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();

            final long tickDistance = MinecraftServer.TICK_MS * 1000000;
            long currentTime;
            while (true) {
                currentTime = System.nanoTime();

                // Keep Alive Handling
                for (Player player : connectionManager.getOnlinePlayers()) {
                    long time = System.currentTimeMillis();
                    if (time - player.getLastKeepAlive() > 10000) {
                        player.refreshKeepAlive(time);
                        KeepAlivePacket keepAlivePacket = new KeepAlivePacket(time);
                        player.getPlayerConnection().sendPacket(keepAlivePacket);
                    }
                }

                // Entities update
                entityManager.update();

                // Blocks update
                instanceManager.updateBlocks();

                // Scheduler
                schedulerManager.update();

                // TODO miscellaneous update (scoreboard)

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

}
