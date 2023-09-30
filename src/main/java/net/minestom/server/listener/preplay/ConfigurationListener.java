package net.minestom.server.listener.preplay;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import org.jetbrains.annotations.NotNull;

public final class ConfigurationListener {

    public static void finishListener(@NotNull ClientFinishConfigurationPacket packet, @NotNull Player player) {
        //todo move to play state
        System.out.println("Finished configuration for " + player.getUsername() );
    }
}
