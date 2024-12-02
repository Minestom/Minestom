package net.minestom.server.inventory.click.integration;


import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@EnvTest
public class LeftClickIntegrationTest {

    @Test
    public void leftSelf(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = player.getInventory();
        var listener = env.listen(InventoryPreClickEvent.class);
        inventory.setItemStack(1, ItemStack.of(Material.DIAMOND));
        // Empty click
        {
            listener.followup(event -> {
                assertNull(event.getInventory()); // Player inventory
                assertEquals(0, event.getSlot());
                assertEquals(ClickType.LEFT_CLICK, event.getClickType());
                assertEquals(ItemStack.AIR, inventory.getCursorItem());
            });
            leftClick(player, 0);
        }
        // Pickup diamond
        {
            listener.followup(event -> {
                assertEquals(1, event.getSlot());
                assertEquals(ItemStack.AIR, inventory.getCursorItem());
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
            });
            leftClick(player, 1);
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getCursorItem());
            assertEquals(ItemStack.AIR, inventory.getItemStack(1));
        }
        // Place it back
        {
            listener.followup(event -> {
                assertEquals(1, event.getSlot());
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getCursorItem());
                assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            });
            leftClick(player, 1);
            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Cancel event
        {
            listener.followup(event -> event.setCancelled(true));
            leftClick(player, 1);
            assertEquals(ItemStack.AIR, inventory.getCursorItem(), "Left click cancellation did not work");
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Change items
        {
            listener.followup(event -> {
                event.setClickedItem(ItemStack.of(Material.DIAMOND, 5));
                event.setCursorItem(ItemStack.of(Material.DIAMOND));
            });
            leftClick(player, 1);
            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND, 6), inventory.getItemStack(1));
        }
    }

    @Test
    public void leftExternal(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = new Inventory(InventoryType.HOPPER, "test");
        player.openInventory(inventory);
        var listener = env.listen(InventoryPreClickEvent.class);
        inventory.setItemStack(1, ItemStack.of(Material.DIAMOND));
        // Empty click in player inv
        {
            listener.followup(event -> {
                assertNull(event.getInventory()); // Player inventory
                assertEquals(0, event.getSlot());
                assertEquals(ClickType.LEFT_CLICK, event.getClickType());
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            });
            leftClick(player, 0);
        }
        // Pickup diamond
        {
            listener.followup(event -> {
                assertEquals(inventory, event.getInventory());
                assertEquals(1, event.getSlot());
                // Ensure that the inventory didn't change yet
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
            });
            leftClickOpenInventory(player, 1);
            // Verify inventory changes
            assertEquals(ItemStack.of(Material.DIAMOND), player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, inventory.getItemStack(1));
        }
        // Place it back
        {
            listener.followup(event -> {
                assertEquals(inventory, event.getInventory());
                assertEquals(1, event.getSlot());
                assertEquals(ItemStack.of(Material.DIAMOND), player.getInventory().getCursorItem());
                assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            });
            leftClickOpenInventory(player, 1);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Cancel event
        {
            listener.followup(event -> event.setCancelled(true));
            leftClickOpenInventory(player, 1);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem(), "Left click cancellation did not work");
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Change items
        {
            listener.followup(event -> {
                assertNull(event.getInventory());
                assertEquals(9, event.getSlot());
                event.setClickedItem(ItemStack.of(Material.DIAMOND, 5));
                event.setCursorItem(ItemStack.of(Material.DIAMOND));
            });
            leftClick(player, 9);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND, 6), player.getInventory().getItemStack(9));
        }
    }

    private void leftClickOpenInventory(Player player, int slot) {
        _leftClick(player.getOpenInventory(), true, player, slot);
    }

    private void leftClick(Player player, int slot) {
        _leftClick(player.getOpenInventory(), false, player, slot);
    }

    private void _leftClick(AbstractInventory openInventory, boolean clickOpenInventory, Player player, int slot) {
        final byte windowId = openInventory != null ? openInventory.getWindowId() : 0;
        if (clickOpenInventory) {
            assert openInventory != null;
            // Do not touch slot
        } else {
            int offset = openInventory != null ? openInventory.getInnerSize() : 0;
            slot = PlayerInventoryUtils.convertMinestomSlotToWindowSlot(slot);
            if (openInventory != null) {
                slot = slot - 9 + offset;
            }
        }
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) slot, (byte) 0,
                ClientClickWindowPacket.ClickType.PICKUP, List.of(), ItemStack.AIR));
        player.interpretPacketQueue();
    }
}
