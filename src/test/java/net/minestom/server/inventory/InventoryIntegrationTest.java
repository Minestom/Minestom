package net.minestom.server.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
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

        ContainerInventory inventory = new ContainerInventory(InventoryType.CHEST_6_ROW, Component.empty());
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

        ContainerInventory inventory = new ContainerInventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setCursorItem(MAGIC_STACK);
        packetTracker.assertSingle(slot -> assertEquals(MAGIC_STACK, slot.itemStack())); // Setting a slot should send a packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setCursorItem(MAGIC_STACK);
        packetTracker.assertEmpty(); // Setting the same slot to the same ItemStack should not send another packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setCursorItem(ItemStack.AIR);
        packetTracker.assertSingle(slot -> assertEquals(ItemStack.AIR, slot.itemStack())); // Setting a slot should send a packet
    }

    @Test
    public void clearInventoryTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        ContainerInventory inventory = new ContainerInventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        var setSlotTracker = connection.trackIncoming(SetSlotPacket.class);

        inventory.setItemStack(1, MAGIC_STACK);
        inventory.setItemStack(3, MAGIC_STACK);
        inventory.setItemStack(19, MAGIC_STACK);
        inventory.setItemStack(40, MAGIC_STACK);
        player.getInventory().setCursorItem(MAGIC_STACK);

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
        final var inventory = new ContainerInventory(InventoryType.CHEST_1_ROW, "title");
        player.openInventory(inventory);
        assertSame(inventory, player.getOpenInventory());
        player.closeInventory();
        assertNull(player.getOpenInventory());
    }

    @Test
    public void openInventoryOnItemAddFromInventoryClosingTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        var listener = env.listen(InventoryItemChangeEvent.class);
        final var firstInventory = new ContainerInventory(InventoryType.CHEST_1_ROW, "title");
        player.openInventory(firstInventory);
        assertSame(firstInventory, player.getOpenInventory());
        player.getInventory().setCursorItem(ItemStack.of(Material.STONE));

        listener.followup();
        player.closeInventory();
        assertNull(player.getOpenInventory());

        player.openInventory(firstInventory);
        player.getInventory().setCursorItem(ItemStack.of(Material.STONE));
        final var secondInventory = new ContainerInventory(InventoryType.CHEST_1_ROW, "title");
        listener.followup(event -> player.openInventory(secondInventory));
        player.closeInventory();
        assertSame(secondInventory, player.getOpenInventory());
    }

    @Test
    public void testInnerInventorySlotSending(Env env) {
        // Inner inventory changes are sent along with the open inventory
        // Otherwise, they are sent separately

        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        Inventory inventory = new ContainerInventory(InventoryType.CHEST_6_ROW, Component.empty());
        player.openInventory(inventory);
        assertEquals(inventory, player.getOpenInventory());

        // Ensure that slots not in the inner inventory are sent separately
        var packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setItemStack(PlayerInventoryUtils.OFF_HAND_SLOT, MAGIC_STACK);
        packetTracker.assertSingle(slot -> {
            assertEquals((byte) 0, slot.windowId());
            assertEquals(PlayerInventoryUtils.OFF_HAND_SLOT, slot.slot());
            assertEquals(MAGIC_STACK, slot.itemStack());
        });

        // Ensure that inner inventory slots are sent as the opened inventory
        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setItemStack(0, MAGIC_STACK); // Test with first inner inventory slot
        packetTracker.assertSingle(slot -> {
            assertEquals(inventory.getWindowId(), slot.windowId());
            assertEquals(PlayerInventoryUtils.minestomToProtocol(0, inventory.getSize()), slot.slot());
            assertEquals(MAGIC_STACK, slot.itemStack());
        });

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setItemStack(35, MAGIC_STACK); // Test with last inner inventory slot
        packetTracker.assertSingle(slot -> {
            assertEquals(inventory.getWindowId(), slot.windowId());
            assertEquals(PlayerInventoryUtils.minestomToProtocol(35, inventory.getSize()), slot.slot());
            assertEquals(MAGIC_STACK, slot.itemStack());
        });
    }

}
