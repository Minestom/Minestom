package net.minestom.server.listener;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;

public class KeepAliveListener {

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        final long packetId = packet.id;
        final long playerId = player.getLastKeepAlive();
        final boolean equals = packetId == playerId;
        if (!equals) {
            player.kick(ColoredText.of(ChatColor.RED + "Bad Keep Alive packet"));
            return;
        }

        player.refreshAnswerKeepAlive(true);

        // Update latency
        final int latency = (int) (System.currentTimeMillis() - packet.id);
        player.refreshLatency(latency);
    }
}
