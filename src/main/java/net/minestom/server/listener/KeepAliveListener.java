package net.minestom.server.listener;

import net.kyori.text.format.TextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;

public class KeepAliveListener {

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        if (packet.id != player.getLastKeepAlive()) {
            player.kick(TextColor.RED + "Bad Keep Alive packet");
            return;
        }

        // Update latency
        int latency = (int) (System.currentTimeMillis() - packet.id);
        player.refreshLatency(latency);
    }
}
