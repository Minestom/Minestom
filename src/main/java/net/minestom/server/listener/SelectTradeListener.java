package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.PlayerSelectTradeEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.client.play.ClientSelectTradePacket;

public class SelectTradeListener {

    public static void listener(ClientSelectTradePacket packet, Player player) {
        final int slot = packet.selectedSlot;

        Inventory inventory = player.getOpenInventory();

        if (inventory == null || inventory.getInventoryType() != InventoryType.MERCHANT) {
            // Inventory not open, or wrong inventory type, ignore packet.
            return;
        }

        EventDispatcher.call(new PlayerSelectTradeEvent(player, inventory, slot));
    }

}
