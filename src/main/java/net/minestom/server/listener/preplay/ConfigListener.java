package net.minestom.server.listener.preplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import org.jetbrains.annotations.NotNull;

public final class ConfigListener {
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void finishConfigListener(@NotNull ClientFinishConfigurationPacket packet, @NotNull Player player) {
        CONNECTION_MANAGER.transitionConfigToPlay(player);
    }
}
