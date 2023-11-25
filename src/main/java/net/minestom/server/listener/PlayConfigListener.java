package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientConfigurationAckPacket;
import org.jetbrains.annotations.NotNull;

public class PlayConfigListener {

    public static void configAckListener(@NotNull ClientConfigurationAckPacket packet, @NotNull Player player) {
        player.startConfigurationPhase();
    }
}
