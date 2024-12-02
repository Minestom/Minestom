package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.book.EditBookEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientEditBookPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

public class BookListener {

    public static void listener(ClientEditBookPacket packet, Player player) {
        int minestomSlot = PlayerInventoryUtils.convertPlayerInventorySlotToMinestomSlot(packet.slot());
        if (!PlayerInventoryUtils.isHotbarOrOffHandSlot(minestomSlot)) return;

        final ItemStack itemStack = player.getInventory().getItemStack(minestomSlot);
        EventDispatcher.call(new EditBookEvent(player, itemStack, packet.pages(), packet.title()));
    }

}
