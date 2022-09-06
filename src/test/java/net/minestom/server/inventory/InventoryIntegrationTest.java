package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class InventoryIntegrationTest {

    private static final ItemStack MAGIC_STACK = ItemStack.of(Material.DIAMOND, 3);

    @Test
    public void setSlotDuplicateTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setItemStack(3, MAGIC_STACK);
        packetTracker.assertSingle(slot -> assertEquals(MAGIC_STACK, slot.itemStack())); // Setting a slot should send a packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setItemStack(3, MAGIC_STACK);
        packetTracker.assertEmpty(); // Setting the same slot to the same ItemStack should not send another packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setItemStack(3, ItemStack.AIR);
        packetTracker.assertSingle(slot -> assertEquals(ItemStack.AIR, slot.itemStack())); // Setting a slot should send a packet
    }

    @Test
    public void setCursorItemDuplicateTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setCursorItem(player, MAGIC_STACK);
        packetTracker.assertSingle(slot -> assertEquals(MAGIC_STACK, slot.itemStack())); // Setting a slot should send a packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setCursorItem(player, MAGIC_STACK);
        packetTracker.assertEmpty(); // Setting the same slot to the same ItemStack should not send another packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        inventory.setCursorItem(player, ItemStack.AIR);
        packetTracker.assertSingle(slot -> assertEquals(ItemStack.AIR, slot.itemStack())); // Setting a slot should send a packet
    }

    @Test
    public void clearInventoryTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        Inventory inventory = new Inventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var setSlotTracker = connection.trackIncoming(SetSlotPacket.class);

        inventory.setItemStack(1, MAGIC_STACK);
        inventory.setItemStack(3, MAGIC_STACK);
        inventory.setItemStack(19, MAGIC_STACK);
        inventory.setItemStack(40, MAGIC_STACK);
        inventory.setCursorItem(player, MAGIC_STACK);

        setSlotTracker.assertCount(5);

        setSlotTracker = connection.trackIncoming(SetSlotPacket.class);
        var updateWindowTracker = connection.trackIncoming(WindowItemsPacket.class);
        var equipmentTracker = connection.trackIncoming(EntityEquipmentPacket.class);

        // Perform the clear operation we are testing
        inventory.clear();

        // Make sure not individual SetSlotPackets get sent
        setSlotTracker.assertEmpty();

        // Make sure WindowItemsPacket is empty
        updateWindowTracker.assertSingle(windowItemsPacket -> {
            assertEquals(ItemStack.AIR, windowItemsPacket.carriedItem());
            for (ItemStack item : windowItemsPacket.items()) {
                assertEquals(ItemStack.AIR, item);
            }
        });

        // Make sure EntityEquipmentPacket isn't sent (this is an Inventory, not a PlayerInventory)
        equipmentTracker.assertEmpty();
    }

    @Test
    public void closeInventoryTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        final var inventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        player.openInventory(inventory);
        assertSame(inventory, player.getOpenInventory());
        player.closeInventory();
        assertNull(player.getOpenInventory());
    }

    @Test
    public void openInventoryOnItemDropFromInventoryClosingTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        var listener = env.listen(ItemDropEvent.class);
        final var firstInventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        player.openInventory(firstInventory);
        assertSame(firstInventory, player.getOpenInventory());
        firstInventory.setCursorItem(player, ItemStack.of(Material.STONE));

        listener.followup();
        player.closeInventory();
        assertNull(player.getOpenInventory());

        player.openInventory(firstInventory);
        firstInventory.setCursorItem(player, ItemStack.of(Material.STONE));
        final var secondInventory = new Inventory(InventoryType.CHEST_1_ROW, "title");
        listener.followup(event -> event.getPlayer().openInventory(secondInventory));
        player.closeInventory();
        assertSame(secondInventory, player.getOpenInventory());
    }

}
