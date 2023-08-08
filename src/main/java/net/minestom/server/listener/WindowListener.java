package net.minestom.server.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryButtonClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.network.packet.client.common.ClientPongPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowButtonPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.server.common.PingPacket;

public class WindowListener {

    public static void clickWindowListener(ClientClickWindowPacket packet, Player player) {
        final int windowId = packet.windowId();
        final Inventory inventory = windowId == 0 ? player.getInventory() : player.getOpenInventory();

        // Prevent some invalid packets
        if (inventory == null || packet.slot() == -1) return;

        var info = player.clickPreprocessor().process(packet, inventory, player.isCreative());
        if (info != null) {
            inventory.handleClick(player, info);
        }

        // (Why is the ping packet necessary?)
        player.sendPacket(new PingPacket((1 << 30) | (windowId << 16)));
    }

    public static void pong(ClientPongPacket packet, Player player) {
        // Empty
    }

    public static void closeWindowListener(ClientCloseWindowPacket packet, Player player) {
        // if windowId == 0 then it is player's inventory, meaning that they hadn't been any open inventory packet
        var openInventory = player.getOpenInventory();
        if (openInventory == null) openInventory = player.getInventory();

        InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(openInventory, player);
        EventDispatcher.call(inventoryCloseEvent);

        player.closeInventory(true);

        Inventory newInventory = inventoryCloseEvent.getNewInventory();
        if (newInventory != null)
            player.openInventory(newInventory);
    }

    public static void buttonClickListener(ClientClickWindowButtonPacket packet, Player player) {
        var openInventory = player.getOpenInventory();
        if (openInventory == null) openInventory = player.getInventory();

        InventoryButtonClickEvent event = new InventoryButtonClickEvent(openInventory, player, packet.buttonId());
        EventDispatcher.call(event);
    }

}
