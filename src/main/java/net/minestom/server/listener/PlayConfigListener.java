package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientConfigurationAckPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ForkJoinPool;

public class PlayConfigListener {
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void configAckListener(@NotNull ClientConfigurationAckPacket packet, @NotNull Player player) {
        CONNECTION_MANAGER.transitionConfigToPlay(player, false);
    }
}
