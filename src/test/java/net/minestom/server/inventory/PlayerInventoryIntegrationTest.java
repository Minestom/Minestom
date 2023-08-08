package net.minestom.server.inventory;

import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PlayerInventoryIntegrationTest {

    private static final ItemStack MAGIC_STACK = ItemStack.of(Material.DIAMOND, 3);

    @Test
    public void setSlotDuplicateTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

        var packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setItemStack(3, MAGIC_STACK);
        packetTracker.assertSingle(slot -> assertEquals(MAGIC_STACK, slot.itemStack())); // Setting a slot should send a packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setItemStack(3, MAGIC_STACK);
        packetTracker.assertEmpty(); // Setting the same slot to the same ItemStack should not send another packet

        packetTracker = connection.trackIncoming(SetSlotPacket.class);
        player.getInventory().setItemStack(3, ItemStack.AIR);
        packetTracker.assertSingle(slot -> assertEquals(ItemStack.AIR, slot.itemStack())); // Setting a slot should send a packet
    }

    @Test
    public void setCursorItemDuplicateTest(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 42, 0)).join();
        assertEquals(instance, player.getInstance());

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

        var setSlotTracker = connection.trackIncoming(SetSlotPacket.class);

        player.getInventory().setItemStack(1, MAGIC_STACK);
        player.getInventory().setItemStack(3, MAGIC_STACK);
        player.getInventory().setItemStack(19, MAGIC_STACK);
        player.getInventory().setItemStack(40, MAGIC_STACK);
        player.getInventory().setCursorItem(MAGIC_STACK);

        setSlotTracker.assertCount(5);

        setSlotTracker = connection.trackIncoming(SetSlotPacket.class);
        var updateWindowTracker = connection.trackIncoming(WindowItemsPacket.class);
        var equipmentTracker = connection.trackIncoming(EntityEquipmentPacket.class);

        // Perform the clear operation we are testing
        player.getInventory().clear();

        // Make sure not individual SetSlotPackets get sent
        setSlotTracker.assertEmpty();

        // Make sure WindowItemsPacket is empty
        updateWindowTracker.assertSingle(windowItemsPacket -> {
            assertEquals(ItemStack.AIR, windowItemsPacket.carriedItem());
            for (ItemStack item : windowItemsPacket.items()) {
                assertEquals(ItemStack.AIR, item);
            }
        });

        // Make sure EntityEquipmentPacket is empty
        equipmentTracker.assertSingle(entityEquipmentPacket -> {
            assertEquals(6, entityEquipmentPacket.equipments().size());
            for (Map.Entry<EquipmentSlot, ItemStack> entry : entityEquipmentPacket.equipments().entrySet()) {
                assertEquals(ItemStack.AIR, entry.getValue());
            }
        });
    }

    @Test
    public void equipmentViewTest(Env env) {
        var instance = env.createFlatInstance();
        var connectionArmored = env.createConnection();
        var playerArmored = connectionArmored.connect(instance, new Pos(0, 42, 0)).join();
        var connectionViewer = env.createConnection();
        var playerViewer = connectionViewer.connect(instance, new Pos(0, 42, 0)).join();

        assertEquals(instance, playerArmored.getInstance());
        assertEquals(instance, playerViewer.getInstance());

        var equipmentTracker = connectionViewer.trackIncoming(EntityEquipmentPacket.class);

        // Setting to an item should send EntityEquipmentPacket to viewer
        playerArmored.setEquipment(EquipmentSlot.HELMET, MAGIC_STACK);
        equipmentTracker.assertSingle(entityEquipmentPacket -> {
            assertEquals(MAGIC_STACK, entityEquipmentPacket.equipments().get(EquipmentSlot.HELMET));
        });

        // Setting to the same item shouldn't send packet
        equipmentTracker = connectionViewer.trackIncoming(EntityEquipmentPacket.class);
        playerArmored.setEquipment(EquipmentSlot.HELMET, MAGIC_STACK);
        equipmentTracker.assertEmpty();

        // Setting to air should send packet
        equipmentTracker = connectionViewer.trackIncoming(EntityEquipmentPacket.class);
        playerArmored.setEquipment(EquipmentSlot.HELMET, ItemStack.AIR);
        equipmentTracker.assertSingle(entityEquipmentPacket -> {
            assertEquals(ItemStack.AIR, entityEquipmentPacket.equipments().get(EquipmentSlot.HELMET));
        });
    }

    @Test
    public void heldItemViewTest(Env env) {
        var instance = env.createFlatInstance();
        var connectionHolder = env.createConnection();
        var playerHolder = connectionHolder.connect(instance, new Pos(0, 42, 0)).join();
        var connectionViewer = env.createConnection();
        var playerViewer = connectionViewer.connect(instance, new Pos(0, 42, 0)).join();

        assertEquals(instance, playerHolder.getInstance());
        assertEquals(instance, playerViewer.getInstance());

        playerHolder.setHeldItemSlot((byte) 0);

        // Setting held item
        var equipmentTracker = connectionViewer.trackIncoming(EntityEquipmentPacket.class);
        playerHolder.setItemInMainHand(MAGIC_STACK);
        equipmentTracker.assertSingle(entityEquipmentPacket -> {
            assertEquals(MAGIC_STACK, entityEquipmentPacket.equipments().get(EquipmentSlot.MAIN_HAND));
        });

        // Changing held slot to an empty slot should update MAIN_HAND to empty item
        equipmentTracker = connectionViewer.trackIncoming(EntityEquipmentPacket.class);
        playerHolder.setHeldItemSlot((byte) 3);
        equipmentTracker.assertSingle(entityEquipmentPacket -> {
            assertEquals(ItemStack.AIR, entityEquipmentPacket.equipments().get(EquipmentSlot.MAIN_HAND));
        });

        // Changing held slot to the original slot should update MAIN_HAND to original item
        equipmentTracker = connectionViewer.trackIncoming(EntityEquipmentPacket.class);
        playerHolder.setHeldItemSlot((byte) 0);
        equipmentTracker.assertSingle(entityEquipmentPacket -> {
            assertEquals(MAGIC_STACK, entityEquipmentPacket.equipments().get(EquipmentSlot.MAIN_HAND));
        });
    }

}
