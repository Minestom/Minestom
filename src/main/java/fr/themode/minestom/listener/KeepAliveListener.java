package fr.themode.minestom.listener;

import fr.themode.minestom.chat.ChatColor;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientKeepAlivePacket;

public class KeepAliveListener {

    public static void listener(ClientKeepAlivePacket packet, Player player) {
        if (packet.id != player.getLastKeepAlive()) {
            player.kick(ChatColor.RED + "Bad Keep Alive packet");
        }
    }

}
