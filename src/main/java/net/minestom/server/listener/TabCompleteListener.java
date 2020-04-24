package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;

public class TabCompleteListener {

    public static void listener(ClientTabCompletePacket packet, Player player) {
        // TODO when is it called?
        System.out.println("text: " + packet.text);
    }


}
