package net.minestom.server.inventory.click.integration;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class ShiftClickIntegrationTest {

    @Test
    public void shiftSelf(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = player.getInventory();
        var listener = env.listen(InventoryPreClickEvent.class);
        // Drag to air
        {
            inventory.setItemStack(0, ItemStack.of(Material.STONE, 64));
            List<InventoryPreClickEvent> events = new ArrayList<>();
            listener.followup(events::add);
            shift(player, 0);
            // start->slot
            assertEquals(2, events.size());
            {
                // Start
                var event = events.get(0);
                assertNull(event.getInventory());
                assertEquals(ClickType.START_SHIFT_CLICK, event.getClickType());
                assertEquals(0, event.getSlot());
                assertEquals(ItemStack.AIR, event.getCursorItem());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getClickedItem());
            }
            {
                // Slot
                var event = events.get(1);
                assertNull(event.getInventory());
                assertEquals(ClickType.SHIFT_CLICK, event.getClickType());
                assertEquals(9, event.getSlot());
                assertEquals(ItemStack.AIR, event.getCursorItem());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getClickedItem());
            }

            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.AIR, inventory.getItemStack(0));
            assertEquals(ItemStack.of(Material.STONE, 64), inventory.getItemStack(9));
        }
    }

    @Test
    public void shiftExternal(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = new Inventory(InventoryType.HOPPER, "test");
        player.openInventory(inventory);
        var listener = env.listen(InventoryPreClickEvent.class);
        // Drag to air
        {
            inventory.setItemStack(0, ItemStack.of(Material.STONE, 64));
            List<InventoryPreClickEvent> events = new ArrayList<>();
            listener.followup(events::add);
            shiftOpenInventory(player, 0);
            // start->slot
            assertEquals(2, events.size());
            {
                // Start
                var event = events.get(0);
                assertEquals(inventory, event.getInventory());
                assertEquals(ClickType.START_SHIFT_CLICK, event.getClickType());
                assertEquals(0, event.getSlot());
                assertEquals(ItemStack.AIR, event.getCursorItem());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getClickedItem());
            }
            {
                // Slot
                var event = events.get(1);
                assertNull(event.getInventory());
                assertEquals(ClickType.SHIFT_CLICK, event.getClickType());
                assertEquals(9, event.getSlot());
                assertEquals(ItemStack.AIR, event.getCursorItem());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getClickedItem());
            }

            assertEquals(ItemStack.AIR, inventory.getCursorItem(player));
            assertEquals(ItemStack.AIR, inventory.getItemStack(0));
            assertEquals(ItemStack.of(Material.STONE, 64), player.getInventory().getItemStack(8));
        }
    }

    private void shiftOpenInventory(Player player, int slot) {
        _shift(player.getOpenInventory(), true, player, slot);
    }

    private void shift(Player player, int slot) {
        _shift(player.getOpenInventory(), false, player, slot);
    }

    private void _shift(Inventory openInventory, boolean clickOpenInventory, Player player, int slot) {
        final byte windowId = openInventory != null ? openInventory.getWindowId() : 0;
        if (clickOpenInventory) {
            assert openInventory != null;
            // Do not touch slot
        } else {
            int offset = openInventory != null ? openInventory.getInnerSize() : 0;
            slot = PlayerInventoryUtils.convertToPacketSlot(slot);
            if (openInventory != null) {
                slot = slot - 9 + offset;
            }
        }

        // button 0 = left
        // button 1 = right
        // They should have identical behavior
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) slot, (byte) 0,
                ClientClickWindowPacket.ClickType.QUICK_MOVE, List.of(), ItemStack.AIR));
        player.interpretPacketQueue();
    }
}
