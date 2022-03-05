package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minestom.server.api.TestUtils.waitUntilCleared;
import static org.junit.jupiter.api.Assertions.*;

public class EventNodeMapTest {
    @Test
    public void map() {
        var item = ItemStack.of(Material.DIAMOND);
        var node = EventNode.all("main");

        AtomicBoolean result = new AtomicBoolean(false);
        var itemNode = EventNode.type("item_node", EventFilter.ITEM);

        assertFalse(node.hasListener(EventNodeTest.ItemTestEvent.class));
        itemNode.addListener(EventNodeTest.ItemTestEvent.class, event -> result.set(true));
        assertDoesNotThrow(() -> node.map(itemNode, item));
        assertTrue(node.hasListener(EventNodeTest.ItemTestEvent.class));

        node.call(new EventNodeTest.ItemTestEvent(item));
        assertTrue(result.get());

        result.set(false);
        node.call(new EventNodeTest.ItemTestEvent(ItemStack.of(Material.GOLD_INGOT)));
        assertFalse(result.get());

        result.set(false);
        assertTrue(node.unmap(item));
        node.call(new EventNodeTest.ItemTestEvent(item));
        assertFalse(result.get());
    }

    @Test
    public void entityLocal() {
        var process = MinecraftServer.updateProcess();
        var node = process.eventHandler();
        var entity = new Entity(EntityType.ZOMBIE);

        AtomicBoolean result = new AtomicBoolean(false);
        var listener = EventListener.of(EventNodeTest.EntityTestEvent.class, event -> result.set(true));

        var handle = node.getHandle(EventNodeTest.EntityTestEvent.class);
        assertFalse(handle.hasListener());
        entity.eventNode().addListener(listener);
        assertTrue(handle.hasListener());

        assertFalse(result.get());

        handle.call(new EventNodeTest.EntityTestEvent(entity));
        assertTrue(result.get());

        result.set(false);
        entity.eventNode().removeListener(listener);

        handle.call(new EventNodeTest.EntityTestEvent(entity));
        assertFalse(result.get());
    }

    @Test
    public void ownerGC() {
        // Ensure that the mapped object gets GCed
        var item = ItemStack.of(Material.DIAMOND);
        var node = EventNode.all("main");
        var itemNode = EventNode.type("item_node", EventFilter.ITEM);
        itemNode.addListener(EventNodeTest.ItemTestEvent.class, event -> {
        });
        node.map(itemNode, item);
        node.call(new EventNodeTest.ItemTestEvent(item));

        var ref = new WeakReference<>(item);
        //noinspection UnusedAssignment
        item = null;
        waitUntilCleared(ref);
    }
}
