package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

public final class CreativeInventoryActionListener {
    public static void listener(ClientCreativeInventoryActionPacket packet, Player player) {
        if (!player.isCreative()) return;
        int slot = PlayerInventoryUtils.protocolToMinestom(packet.slot());
        final ItemStack item = packet.item();
        if (slot == -1) {
            player.getInventory().handleClick(player, new ClickInfo.CreativeDropItem(item));
        } else {
            player.getInventory().handleClick(player, new ClickInfo.CreativeSetItem(slot, item));
        }
    }
}
