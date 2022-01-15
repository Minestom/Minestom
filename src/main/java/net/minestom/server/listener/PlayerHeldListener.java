package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.network.packet.client.play.ClientHeldItemChangePacket;
import net.minestom.server.utils.MathUtils;

public class PlayerHeldListener {

    public static void heldListener(ClientHeldItemChangePacket packet, Player player) {
        if (!MathUtils.isBetween(packet.slot(), 0, 8)) {
            // Incorrect packet, ignore
            return;
        }

        final byte slot = (byte) packet.slot();

        PlayerChangeHeldSlotEvent changeHeldSlotEvent = new PlayerChangeHeldSlotEvent(player, slot);
        EventDispatcher.call(changeHeldSlotEvent);

        if (!changeHeldSlotEvent.isCancelled()) {
            // Event hasn't been canceled, process it

            final byte resultSlot = changeHeldSlotEvent.getSlot();

            // If the held slot has been changed by the event, send the change to the player
            if (resultSlot != slot) {
                player.setHeldItemSlot(resultSlot);
            } else {
                // Otherwise, simply refresh the player field
                player.refreshHeldSlot(resultSlot);
            }
        } else {
            // Event has been canceled, send the last held slot to refresh the client
            player.setHeldItemSlot(player.getHeldSlot());
        }
    }

}
