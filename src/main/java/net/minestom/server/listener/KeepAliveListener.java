package net.minestom.server.listener;

import net.kyori.text.TextComponent;
import net.kyori.text.format.TextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;

public class KeepAliveListener {

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        final long packetId = packet.id;
        final long playerId = player.getLastKeepAlive();
        final boolean equals = packetId == playerId;
        if (!equals) {
            TextComponent textComponent = TextComponent.of("Bad Keep Alive packet")
                    .color(TextColor.RED);
            player.kick(textComponent);
            return;
        }

        // Update latency
        int latency = (int) (System.currentTimeMillis() - packet.id);
        player.refreshLatency(latency);
    }
}
