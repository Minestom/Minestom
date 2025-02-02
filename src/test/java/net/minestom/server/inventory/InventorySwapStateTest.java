package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventorySwapEvent;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class InventorySwapStateTest {

    @Test
    public void doNotMismatchOldAndNewInventoryOnSwappingInventories(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        final var eventRef = new AtomicReference<InventorySwapEvent>();
        final var eventNode = EventNode.all("inventory-swap-test");
        eventNode.addListener(InventorySwapEvent.class, event -> {
            eventRef.set(event);
            MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
        });
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);

        var firstInventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("First inventory"));
        player.openInventory(firstInventory);
        var secondInventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("Second inventory"));
        player.openInventory(secondInventory);

        final var event = eventRef.get();
        assertNotNull(event, "Event wasn't called");
        assertNotNull(event.getOldInventory(), "Could not get old inventory.");
        assertNotNull(event.getNewInventory(), "Could not get new inventory.");
        assertNotNull(event.getPlayer(), "Could not get player.");
        assertEquals(player, event.getPlayer(), "Wrong player in event.");
        assertNotNull(event.getPlayer().getOpenInventory(), "Could not get players open inventory.");
        assertNotEquals(firstInventory, event.getPlayer().getOpenInventory(), "First inventory should not be the open inventory.");
        assertEquals(secondInventory, event.getPlayer().getOpenInventory(), "Second inventory should be the open inventory.");
        assertEquals(firstInventory, event.getOldInventory(), "First opened inventory should be the old inventory.");
        assertEquals(secondInventory, event.getNewInventory(), "Second opened inventory should be the new inventory.");
        assertNotEquals(event.getOldInventory(), event.getNewInventory(), "Old inventory should not be the new inventory.");
    }

    @Test
    public void doNotFireInventoryCloseOnInventorySwap(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        final var eventRef = new AtomicReference<InventoryCloseEvent>();
        final var eventNode = EventNode.all("inventory-no-close-test");
        eventNode.addListener(InventoryCloseEvent.class, event -> {
            eventRef.set(event);
            MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
        });
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);

        var firstInventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("First inventory"));
        player.openInventory(firstInventory);
        var secondInventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("Second inventory"));
        player.openInventory(secondInventory);

        final var event = eventRef.get();
        assertNull(event, "Event was called");
    }
}
