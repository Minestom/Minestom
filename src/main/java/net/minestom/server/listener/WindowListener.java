package net.minestom.server.listener;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.common.ClientPongPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.server.common.PingPacket;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;
import org.jetbrains.annotations.Nullable;

public class WindowListener {

    public static void clickWindowListener(ClientClickWindowPacket packet, Player player) {
        final int windowId = packet.windowId();
        final boolean playerInventory = windowId == 0;
        final AbstractInventory inventory = playerInventory ? player.getInventory() : player.getOpenInventory();

        // Prevent some invalid packets
        if (inventory == null || packet.slot() == -1) return;

        // Process the click
        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
        @Nullable Integer size = playerInventory ? null : inventory.getSize();

        Click.Info result = player.getClickPreprocessor().processClick(packet, isCreative, size);

        boolean successful = false;
        if (result != null) {
            successful = inventory.handleClick(player, result);
        }

        // Prevent ghost item when the click is cancelled
        if (!successful) {
            player.getInventory().update(player);
            if (!playerInventory) {
                inventory.update(player);
            }
        }

        // Prevent the player from picking a ghost item in cursor
        ItemStack cursorItem = player.getInventory().getCursorItem();
        player.sendPacket(new SetCursorItemPacket(cursorItem));

        // (Why is the ping packet necessary?)
        player.sendPacket(new PingPacket((1 << 30) | (windowId << 16)));
    }

    public static void pong(ClientPongPacket packet, Player player) {
        // Empty
    }

    public static void closeWindowListener(ClientCloseWindowPacket packet, Player player) {
        // if windowId == 0 then it is player's inventory, meaning that they hadn't been any open inventory packet
        player.closeInventory(true);
    }

}
