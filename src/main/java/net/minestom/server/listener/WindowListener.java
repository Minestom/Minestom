package net.minestom.server.listener;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.common.ClientPongPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.server.common.PingPacket;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;

public class WindowListener {

    public static void clickWindowListener(ClientClickWindowPacket packet, Player player) {
        final int windowId = packet.windowId();
        final AbstractInventory inventory = windowId == 0 ? player.getInventory() : player.getOpenInventory();
        if (inventory == null) {
            // Invalid packet
            return;
        }

        final short slot = packet.slot();
        final byte button = packet.button();
        final ClientClickWindowPacket.ClickType clickType = packet.clickType();

        boolean successful = false;

        // prevent click in a non-interactive slot (why does it exist?)
        if (slot == -1) {
            return;
        }
        if (clickType == ClientClickWindowPacket.ClickType.PICKUP) {
            if (button == 0) {
                if (slot != -999) {
                    successful = inventory.leftClick(player, slot);
                } else {
                    successful = inventory.drop(player, true, slot, button);
                }
            } else if (button == 1) {
                if (slot != -999) {
                    successful = inventory.rightClick(player, slot);
                } else {
                    successful = inventory.drop(player, false, slot, button);
                }
            }
        } else if (clickType == ClientClickWindowPacket.ClickType.QUICK_MOVE) {
            successful = inventory.shiftClick(player, slot);
        } else if (clickType == ClientClickWindowPacket.ClickType.SWAP) {
            if (slot < 0 || button < 0) return;
            successful = inventory.changeHeld(player, slot, button);
        } else if (clickType == ClientClickWindowPacket.ClickType.CLONE) {
            successful = player.getGameMode() == GameMode.CREATIVE;
            if (successful) {
                player.getInventory().setCursorItem(packet.clickedItem());
            }
        } else if (clickType == ClientClickWindowPacket.ClickType.THROW) {
            successful = inventory.drop(player, false, slot, button);
        } else if (clickType == ClientClickWindowPacket.ClickType.QUICK_CRAFT) {
            successful = inventory.dragging(player, slot, button);
        } else if (clickType == ClientClickWindowPacket.ClickType.PICKUP_ALL) {
            successful = inventory.doubleClick(player, slot);
        }

        // Prevent ghost item when the click is cancelled
        if (!successful) {
            player.getInventory().update();
            if (inventory instanceof Inventory) {
                ((Inventory) inventory).update(player);
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
        InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(player.getOpenInventory(), player);
        EventDispatcher.call(inventoryCloseEvent);

        player.closeInventory(true);

        Inventory newInventory = inventoryCloseEvent.getNewInventory();
        if (newInventory != null)
            player.openInventory(newInventory);
    }

}
