package net.minestom.server.listener;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

public class CreativeInventoryActionListener {

    public static void listener(ClientCreativeInventoryActionPacket packet, Player player) {
        if (player.getGameMode() != GameMode.CREATIVE)
            return;

        short slot = packet.slot;
        final ItemStack item = packet.item;

        if (slot != -1) {
            // Set item
            slot = (short) PlayerInventoryUtils.convertSlot(slot, PlayerInventoryUtils.OFFSET);
            PlayerInventory inventory = player.getInventory();
            inventory.setItemStack(slot, item);
        } else {
            // Drop item
            player.dropItem(item);
        }

    }

}
