package net.minestom.server.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;

public final class KeepAliveListener {
    private static final Component KICK_MESSAGE = Component.text("Bad Keep Alive packet", NamedTextColor.RED);

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        final long packetId = packet.id();
        if (packetId != player.getLastKeepAlive()) {
            player.kick(KICK_MESSAGE);
            return;
        }
        player.refreshAnswerKeepAlive(true);
        // Update latency
        final int latency = (int) (System.currentTimeMillis() - packetId);
        player.refreshLatency(latency);
    }
}
