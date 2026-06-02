package net.minestom.server.listener;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryButtonClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.common.ClientPongPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowButtonPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
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
        @Nullable Integer size = playerInventory ? null : inventory.getSize();
        Click click = player.getClickPreprocessor().processClick(packet, size);

        boolean successful = true;
        check: if (click != null) {
            // Disallow creative clicks when not in creative
            boolean isNotCreative = player.getGameMode() != GameMode.CREATIVE;
            if (isNotCreative && player.getClickPreprocessor().isCreativeClick(click, !player.getInventory().getCursorItem().isAir())) {
                successful = false;
                break check;
            }

            // Reset the didCloseInventory field
            // Wait for events to possibly close the inventory
            player.UNSAFE_changeDidCloseInventory(false);

            Click.Window window = Click.toWindow(click, size);
            // Call InventoryPreClickEvent
            InventoryPreClickEvent inventoryPreClickEvent = new InventoryPreClickEvent(window.inOpened() ? inventory : player.getInventory(), player, window.click());
            EventDispatcher.call(inventoryPreClickEvent);

            click = Click.fromWindow(new Click.Window(window.inOpened(), inventoryPreClickEvent.getClick()), size);

            if (player.didCloseInventory()) {
                // Cancel the click if the inventory has been closed by Player#closeInventory
                player.UNSAFE_changeDidCloseInventory(false);
                successful = false;
            } else if (inventoryPreClickEvent.isCancelled()) {
                // Cancel it if the event is cancelled and we haven't already done that
                successful = false;
            } else {
                successful = inventory.handleClick(player, click);
            }
        }

        // Prevent ghost item when the click is cancelled
        if (!successful) {
            player.getInventory().update(player);
            if (!playerInventory) {
                inventory.update(player);
            }
        }

        // Resync in case the client sent item does not match what we think it should be.
        ItemStack cursorItem = player.getInventory().getCursorItem();
        if (!ItemStack.Hash.of(cursorItem).equals(packet.clickedItem()))
            player.sendPacket(new SetCursorItemPacket(cursorItem));
    }

    public static void pong(ClientPongPacket packet, Player player) {
        // Empty
    }

    public static void closeWindowListener(ClientCloseWindowPacket packet, Player player) {
        player.closeInventory(true, (byte) packet.windowId());
    }

    public static void inventoryButtonClickListener(ClientClickWindowButtonPacket packet, Player player) {
        AbstractInventory inventory = player.getOpenInventory();

        // Can't press a button if the inventory is not open
        if (inventory == null) return;

        // Can't press a button if the inventory is different from the packet's window id
        if (packet.windowId() != (int) inventory.getWindowId()) return;

        EventDispatcher.call(new InventoryButtonClickEvent(player, inventory, packet.buttonId()));
    }
}
