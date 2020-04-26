package net.minestom.server.listener;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientKeepAlivePacket;

public class KeepAliveListener {

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        if (packet.id != player.getLastKeepAlive()) {
            player.kick(ChatColor.RED + "Bad Keep Alive packet");
            return;
        }

        // Update latency
        int latency = (int) (System.currentTimeMillis() - packet.id);
        player.refreshLatency(latency);
    }

}
