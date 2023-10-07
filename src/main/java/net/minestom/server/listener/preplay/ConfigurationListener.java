package net.minestom.server.listener.preplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import org.jetbrains.annotations.NotNull;

public final class ConfigurationListener {

    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    public static void finishListener(@NotNull ClientFinishConfigurationPacket packet, @NotNull Player player) {
        player.getPlayerConnection().setClientState(ConnectionState.PLAY);

        System.out.println("Finished configuration for " + player.getUsername() );
        CONNECTION_MANAGER.startPlayState(player);

        //todo move to play state

    }
}
