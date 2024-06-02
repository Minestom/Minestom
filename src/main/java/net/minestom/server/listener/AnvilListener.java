package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerAnvilInputEvent;
import net.minestom.server.inventory.ContainerInventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.client.play.ClientNameItemPacket;
import org.jetbrains.annotations.NotNull;

public final class AnvilListener {

    public static void nameItemListener(@NotNull ClientNameItemPacket packet, @NotNull Player player) {
        if (!(player.getOpenInventory() instanceof ContainerInventory openInventory))
            return;
        if (openInventory.getInventoryType() != InventoryType.ANVIL)
            return;

        EventDispatcher.call(new PlayerAnvilInputEvent(player, packet.itemName()));
    }

    private AnvilListener() {
    }

}
