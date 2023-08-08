package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;

public final class CreativeInventoryActionListener {
    public static void listener(ClientCreativeInventoryActionPacket packet, Player player) {
        if (!player.isCreative()) return;
        short slot = packet.slot();
        final ItemStack item = packet.item();
        if (slot == -1) {
            player.dropItem(item);
        } else {
            player.getInventory().setItemStack(slot, item);
        }
    }
}
