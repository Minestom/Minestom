package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientConfigurationAckPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ForkJoinPool;

public class PlayConfigListener {

    public static void configAckListener(@NotNull ClientConfigurationAckPacket packet, @NotNull Player player) {
//        player.startConfigurationPhase();
        System.out.println("PLAYER HAS REENTERED CONFIGURATION PHASE!!!!!" + player.getUsername());
        ForkJoinPool.commonPool().execute(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            player.startConfigurationPhase();
        });
    }
}
