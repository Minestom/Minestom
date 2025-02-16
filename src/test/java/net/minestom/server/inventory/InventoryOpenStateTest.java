package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryOpenEvent;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class InventoryOpenStateTest {

    @Test
    public void doNotMismatchOldAndNewInventoryOnSwappingInventories(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0));
        assertEquals(instance, player.getInstance());

        final var eventRef = new AtomicReference<InventoryOpenEvent>();
        final var eventNode = EventNode.all("inventory-swap-test");
        eventNode.addListener(InventoryOpenEvent.class, eventRef::set);
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);

        // We are asserting, after the processing of the InventoryOpenEvent,
        // so the event.getInventory() and event.getOldInventory() should always
        // be the same as the inventory passen on player.openInventory(Inventory inventory)

        var firstInventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("First inventory"));
        player.openInventory(firstInventory);

        final var firstEvent = eventRef.get();
        assertNotNull(firstEvent, "Event wasn't called");
        assertNotNull(firstEvent.getInventory(), "Opened inventory was null.");
        assertNotNull(firstEvent.getOldInventory(), "Old inventory wasn't null.");
        assertEquals(firstInventory, firstEvent.getInventory(), "Opened inventory wasn't the first inventory");
        assertEquals(firstInventory, firstEvent.getOldInventory(), "Old inventory wasn't the second inventory");

        var secondInventory = new Inventory(InventoryType.CHEST_2_ROW, Component.text("Second inventory"));
        player.openInventory(secondInventory);

        final var secondEvent = eventRef.get();
        assertNotNull(secondEvent, "Event wasn't called");
        assertNotNull(secondEvent.getInventory(), "Opened inventory was null.");
        assertNotNull(secondEvent.getOldInventory(), "Old inventory was null.");
        assertEquals(secondInventory, secondEvent.getInventory(), "Opened inventory wasn't the second inventory");
        assertEquals(secondInventory, secondEvent.getOldInventory(), "Old inventory wasn't the second inventory");

        MinecraftServer.getGlobalEventHandler().removeChild(eventNode);
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
