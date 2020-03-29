package fr.themode.minestom.listener;

import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.client.play.ClientCreativeInventoryActionPacket;

import static fr.themode.minestom.utils.inventory.PlayerInventoryUtils.OFFSET;
import static fr.themode.minestom.utils.inventory.PlayerInventoryUtils.convertSlot;

public class CreativeInventoryActionListener {

    public static void listener(ClientCreativeInventoryActionPacket packet, Player player) {
        if (player.getGameMode() != GameMode.CREATIVE)
            return;
        ItemStack item = packet.item;
        short slot = packet.slot;
        PlayerInventory inventory = player.getInventory();
        inventory.setItemStack(convertSlot(slot, OFFSET), item);

    }

}
