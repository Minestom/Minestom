package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;

public class PlayerHeldListener {

    public static void heldListener(ClientHeldItemChangePacket packet, Player player) {
        short slot = packet.slot;
        if (slot < 0 || slot > 8)
            return;
        player.refreshHeldSlot(slot);
    }

}
