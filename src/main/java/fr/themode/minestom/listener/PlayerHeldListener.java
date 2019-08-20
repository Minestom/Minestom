package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.play.ClientHeldItemChangePacket;

public class PlayerHeldListener {

    public static void heldListener(ClientHeldItemChangePacket packet, Player player) {
        short slot = packet.slot;
        if (slot < 0 || slot > 8)
            return;
        player.refreshHeldSlot(slot);
    }

}
