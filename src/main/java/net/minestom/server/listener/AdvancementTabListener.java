package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientAdvancementTabPacket;

public class AdvancementTabListener {

    public static void listener(ClientAdvancementTabPacket packet, Player player) {
        // Currentely unused and don't see much usage for an API
        // TODO: Create an Event?
    }
}
