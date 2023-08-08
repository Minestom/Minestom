package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

public final class CreativeInventoryActionListener {
    public static void listener(ClientCreativeInventoryActionPacket packet, Player player) {
        if (!player.isCreative()) return;

        ItemStack item = packet.item();

        if (packet.slot() == -1) { // -1 here indicates a drop
            player.getInventory().handleClick(player, new Click.Info.CreativeDropItem(item));
        }

        int slot = PlayerInventoryUtils.protocolToMinestom(packet.slot());
        if (slot == -1) return; // -1 after conversion indicates an invalid slot

        player.getInventory().handleClick(player, new Click.Info.CreativeSetItem(slot, item));
    }
}
