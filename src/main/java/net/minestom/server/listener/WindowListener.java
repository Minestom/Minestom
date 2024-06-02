package net.minestom.server.listener;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.inventory.InventoryButtonClickEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.network.packet.client.common.ClientPongPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowButtonPacket;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.server.common.PingPacket;
import net.minestom.server.utils.inventory.ClickUtils;

import java.util.List;

public class WindowListener {

    public static void clickWindowListener(ClientClickWindowPacket packet, Player player) {
        final int windowId = packet.windowId();
        final boolean playerInventory = windowId == 0;
        final Inventory inventory = playerInventory ? player.getInventory() : player.getOpenInventory();

        // Prevent some invalid packets
        if (inventory == null || packet.slot() == -1) return;

        Click.Preprocessor preprocessor = player.clickPreprocessor();
        final Click.Info info = preprocessor.processClick(packet, player.getGameMode() == GameMode.CREATIVE, playerInventory ? null : inventory.getSize());
        if (info != null) {
            Click.Getter getter = ClickUtils.makeGetter(inventory, player.getInventory());
            List<Click.Change> clientPrediction = ClickUtils.packetToChanges(packet, info, getter, playerInventory);

            inventory.handleClick(player, info, clientPrediction);
        }

        // (Why is the ping packet necessary?)
        player.sendPacket(new PingPacket((1 << 30) | (windowId << 16)));
    }

    public static void pong(ClientPongPacket packet, Player player) {
        // Empty
    }

    public static void closeWindowListener(ClientCloseWindowPacket packet, Player player) {
        var openInventory = player.getOpenInventory();

        // The client sends a packet if they close their own inventory, but this means nothing, it isn't cancellable,
        // and we can't reopen their inventory for them. Essentially, it's useless, and is irrelevant here.
        if (openInventory == null) return;

        InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(openInventory, player);
        EventDispatcher.call(inventoryCloseEvent);

        // If an event listener opened an inventory, exit
        if (player.getOpenInventory() != openInventory) return;

        if (inventoryCloseEvent.isCancelled()) {
            // Fake an inventory close
            player.UNSAFE_changeSkipClosePacket(true);
            openInventory.removeViewer(player);
            openInventory.addViewer(player);
        } else {
            player.closeInventory(true);
        }

    }

    public static void buttonClickListener(ClientClickWindowButtonPacket packet, Player player) {
        var openInventory = player.getOpenInventory();
        if (openInventory == null) openInventory = player.getInventory();

        InventoryButtonClickEvent event = new InventoryButtonClickEvent(openInventory, player, packet.buttonId());
        EventDispatcher.call(event);
    }
}
