package fr.themode.minestom;

import fr.themode.minestom.entity.EntityManager;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.InstanceManager;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.packet.server.play.KeepAlivePacket;
import fr.themode.minestom.timer.SchedulerManager;
import fr.themode.minestom.utils.thread.MinestomThread;

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
                    long time = currentTime / 1_000_000;
                    if (time - player.getLastKeepAlive() > 20000) {
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
