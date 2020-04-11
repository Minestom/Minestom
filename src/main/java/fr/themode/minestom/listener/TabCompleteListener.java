package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientTabCompletePacket;

public class TabCompleteListener {

    public static void listener(ClientTabCompletePacket packet, Player player) {
        // TODO when is it called?
        System.out.println("text: " + packet.text);
    }


}
