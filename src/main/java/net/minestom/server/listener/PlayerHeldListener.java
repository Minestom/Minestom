package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.server.utils.MathUtils;

public class PlayerHeldListener {

    public static void heldListener(ClientHeldItemChangePacket packet, Player player) {
        short slot = packet.slot;
        if (!MathUtils.isBetween(slot, 0, 8)) {
            // Incorrect packet, ignore
            return;
        }
        player.refreshHeldSlot(slot);
    }

}
