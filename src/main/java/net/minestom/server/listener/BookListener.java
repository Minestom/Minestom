package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.book.EditBookEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientEditBookPacket;

public class BookListener {

    public static void listener(ClientEditBookPacket packet, Player player) {
        ItemStack itemStack = player.getInventory().getItemStack(packet.slot());
        EventDispatcher.call(new EditBookEvent(player, itemStack, packet.pages(), packet.title()));
    }

}
