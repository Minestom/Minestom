package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.client.play.ClientPongPacket;
import net.minestom.server.network.packet.server.play.PingPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;

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

        //System.out.println("Window id: " + windowId + " | slot: " + slot + " | button: " + button + " | clickType: " + clickType);

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
            successful = inventory.changeHeld(player, slot, button);
        } else if (clickType == ClientClickWindowPacket.ClickType.CLONE) {
            successful = player.isCreative();
            if (successful) {
                setCursor(player, inventory, packet.clickedItem());
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
        refreshCursorItem(player, inventory);

        // (Why is the ping packet necessary?)
        player.getPlayerConnection().sendPacket(new PingPacket((1 << 30) | (windowId << 16)));
    }

    public static void pong(ClientPongPacket packet, Player player) {
        // Empty
    }

    public static void closeWindowListener(ClientCloseWindowPacket packet, Player player) {
        // if windowId == 0 then it is player's inventory, meaning that they hadn't been any open inventory packet
        InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(player.getOpenInventory(), player);
        EventDispatcher.call(inventoryCloseEvent);

        player.closeInventory();

        Inventory newInventory = inventoryCloseEvent.getNewInventory();
        if (newInventory != null)
            player.openInventory(newInventory);
    }

    /**
     * @param player    the player to refresh the cursor item
     * @param inventory the player open inventory, null if not any (could be player inventory)
     */
    private static void refreshCursorItem(Player player, AbstractInventory inventory) {
        ItemStack cursorItem;
        if (inventory instanceof PlayerInventory) {
            cursorItem = ((PlayerInventory) inventory).getCursorItem();
        } else if (inventory instanceof Inventory) {
            cursorItem = ((Inventory) inventory).getCursorItem(player);
        } else {
            throw new RuntimeException("Invalid inventory: " + inventory.getClass());
        }
        final SetSlotPacket setSlotPacket = SetSlotPacket.createCursorPacket(cursorItem);
        player.getPlayerConnection().sendPacket(setSlotPacket);
    }

    private static void setCursor(Player player, AbstractInventory inventory, ItemStack itemStack) {
        if (inventory instanceof PlayerInventory) {
            ((PlayerInventory) inventory).setCursorItem(itemStack);
        } else if (inventory instanceof Inventory) {
            ((Inventory) inventory).setCursorItem(player, itemStack);
        } else {
            throw new RuntimeException("Invalid inventory: " + inventory.getClass());
        }
    }
}
