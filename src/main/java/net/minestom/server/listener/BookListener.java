package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.book.EditBookEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientEditBookPacket;

public class BookListener {

    public static void listener(ClientEditBookPacket packet, Player player) {
        int slot = packet.slot();
        if (slot < 0 || slot > 8) return;

        // Do not need to convert slot as hotbar slots correspond to Minestom inventory slots
        ItemStack itemStack = player.getInventory().getItemStack(slot);
        EventDispatcher.call(new EditBookEvent(player, itemStack, packet.pages(), packet.title()));
    }

}
