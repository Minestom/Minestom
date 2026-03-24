package net.minestom.server.inventory.click.integration;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class LeftClickIntegrationTest {

    @Test
    public void leftSelf(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = player.getInventory();
        var listener = env.listen(InventoryPreClickEvent.class);
        inventory.setItemStack(1, ItemStack.of(Material.DIAMOND));
        inventory.setItemStack(2, ItemStack.of(Material.DIAMOND_HELMET));
        inventory.setItemStack(3, ItemStack.of(Material.SHIELD));
        inventory.setItemStack(4, ItemStack.builder(Material.COOKED_BEEF).set(DataComponents.EQUIPPABLE,
                new Equippable(EquipmentSlot.LEGGINGS, Equippable.DEFAULT_EQUIP_SOUND, null, null, null,
                        true, true, true, true, true, Equippable.DEFAULT_SHEARING_SOUND)).build());
        // Empty click
        {
            listener.followup(event -> {
                assertEquals(event.getInventory(), inventory);
                assertEquals(new Click.Left(0), event.getClick());
                assertEquals(ItemStack.AIR, inventory.getCursorItem());
            });
            leftClick(player, 0);
        }
        // Pickup diamond
        {
            listener.followup(event -> {
                assertEquals(new Click.Left(1), event.getClick());
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
                assertEquals(new Click.Left(1), event.getClick());
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getCursorItem());
                assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            });
            leftClick(player, 1);
            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Shift click an armor item into the armor slot
        {
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(2), event.getClick());
                assertEquals(ItemStack.of(Material.DIAMOND_HELMET), player.getInventory().getItemStack(2));
            });
            shiftClick(player, 2);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, player.getInventory().getItemStack(2));
            assertEquals(ItemStack.of(Material.DIAMOND_HELMET), player.getHelmet());
        }
        // Shift click non armor material but equippable item into the armor slot
        {
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(4), event.getClick());
                assertEquals(Material.COOKED_BEEF, player.getInventory().getItemStack(4).material());
            });
            shiftClick(player, 4);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, player.getInventory().getItemStack(4));
            assertEquals(Material.COOKED_BEEF, player.getLeggings().material());
        }
        // Shift click an armor slot item back into the inventory
        {
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(41), event.getClick());
                assertEquals(ItemStack.of(Material.DIAMOND_HELMET), player.getHelmet());
            });
            shiftClick(player, 41);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.of(Material.AIR), player.getHelmet());
            assertEquals(ItemStack.of(Material.DIAMOND_HELMET), player.getInventory().getItemStack(9));
        }
        // Shift click a shield into the off-hand slot
        {
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(3), event.getClick());
                assertEquals(ItemStack.of(Material.SHIELD), player.getInventory().getItemStack(3));
            });
            shiftClick(player, 3);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, player.getInventory().getItemStack(3));
            assertEquals(ItemStack.of(Material.SHIELD), player.getInventory().getItemStack(45));
        }
        // Shift click a shield in the off-hand slot to the inventory
        {
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(45), event.getClick());
                assertEquals(ItemStack.of(Material.SHIELD), player.getInventory().getItemStack(45));
            });
            shiftClick(player, 45);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, player.getInventory().getItemStack(45));
            assertEquals(ItemStack.of(Material.SHIELD), player.getInventory().getItemStack(10));
        }
        // Shift click a player crafting inventory ingredient to the player inventory
        {
            player.getInventory().setItemStack(37, ItemStack.of(Material.GOLDEN_HELMET));
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(37), event.getClick());
                assertEquals(ItemStack.of(Material.GOLDEN_HELMET), player.getInventory().getItemStack(37));
            });
            shiftClick(player, 37);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, player.getInventory().getItemStack(37));
            assertEquals(ItemStack.of(Material.GOLDEN_HELMET), player.getInventory().getItemStack(11));
        }
        // Shift click a player crafting inventory result to the player hotbar
        {
            player.getInventory().setItemStack(36, ItemStack.of(Material.IRON_HELMET));
            listener.followup(event -> {
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(36), event.getClick());
                assertEquals(ItemStack.of(Material.IRON_HELMET), player.getInventory().getItemStack(36));
            });
            shiftClick(player, 36);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.AIR, player.getInventory().getItemStack(36));
            assertEquals(ItemStack.of(Material.IRON_HELMET), player.getInventory().getItemStack(8));
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
                Click.Left left = assertInstanceOf(Click.Left.class, event.getClick());

                inventory.setItemStack(left.slot(), ItemStack.of(Material.DIAMOND, 5));
                inventory.setCursorItem(ItemStack.of(Material.DIAMOND));
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
                assertEquals(player.getInventory(), event.getInventory());
                assertEquals(new Click.Left(0), event.getClick());
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            });
            leftClick(player, 0);
        }
        // Pickup diamond
        {
            listener.followup(event -> {
                assertEquals(inventory, event.getInventory());
                assertEquals(new Click.Left(1), event.getClick());
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
                assertEquals(new Click.Left(1), event.getClick());
                assertEquals(ItemStack.of(Material.DIAMOND), player.getInventory().getCursorItem());
                assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            });
            leftClickOpenInventory(player, 1);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
        }
        // Shift click the item into the player's inventory
        {
            listener.followup(event -> {
                assertEquals(inventory, event.getInventory());
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(1), event.getClick());
                assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(1));
            });
            shiftClickOpenInventory(player, 1);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem()); // When shift-clicking, the cursor item shouldn't change
            assertEquals(ItemStack.of(Material.DIAMOND), player.getInventory().getItemStack(8)); // The item should appear in the player's last hotbar slot
        }
        // Shift click the item back into the external inventory
        {
            listener.followup(event -> {
                assertEquals(player.getInventory(), event.getInventory());
                assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
                assertEquals(new Click.LeftShift(8), event.getClick());
                assertEquals(ItemStack.of(Material.DIAMOND), player.getInventory().getItemStack(8));
            });
            shiftClick(player, 8);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem()); // When shift-clicking, the cursor item shouldn't change
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(0)); // The item should appear in the external inventory's first slot
        }
        // Shift click into the player's inventory when their hotbar is full
        {
            inventory.setItemStack(1, ItemStack.of(Material.GOLD_INGOT));
            for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                player.getInventory().setItemStack(hotbarSlot, ItemStack.of(Material.BRICK));
            }
            listener.followup(event -> {
                assertEquals(inventory, event.getInventory());
            });
            shiftClickOpenInventory(player, 1);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.of(Material.GOLD_INGOT), player.getInventory().getItemStack(35)); // The item should appear in the bottom right of the player's inventory excluding the hotbar
        }
        // Cancel event
        {
            listener.followup(event -> event.setCancelled(true));
            leftClickOpenInventory(player, 0);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem(), "Left click cancellation did not work");
            assertEquals(ItemStack.of(Material.DIAMOND), inventory.getItemStack(0));
        }
        // Change items
        {
            listener.followup(event -> {
                assertEquals(player.getInventory(), event.getInventory());
                assertEquals(new Click.Left(9), event.getClick());

                Click.Left left = assertInstanceOf(Click.Left.class, event.getClick());

                event.getInventory().setItemStack(left.slot(), ItemStack.of(Material.DIAMOND, 5));
                player.getInventory().setCursorItem(ItemStack.of(Material.DIAMOND));
            });
            leftClick(player, 9);
            assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
            assertEquals(ItemStack.of(Material.DIAMOND, 6), player.getInventory().getItemStack(9));
        }
    }

    private void shiftClickOpenInventory(Player player, int slot) {
        _leftClick(player.getOpenInventory(), true, player, slot, true);
    }

    private void shiftClick(Player player, int slot) {
        _leftClick(player.getOpenInventory(), false, player, slot, true);
    }

    private void leftClickOpenInventory(Player player, int slot) {
        _leftClick(player.getOpenInventory(), true, player, slot, false);
    }

    private void leftClick(Player player, int slot) {
        _leftClick(player.getOpenInventory(), false, player, slot, false);
    }

    private void _leftClick(AbstractInventory openInventory, boolean clickOpenInventory, Player player, int slot, boolean shift) {
        final byte windowId = openInventory != null ? openInventory.getWindowId() : 0;
        if (clickOpenInventory) {
            assertNotNull(openInventory);
            // Do not touch slot
        } else {
            int offset = openInventory != null ? openInventory.getInnerSize() : 0;
            slot = PlayerInventoryUtils.convertMinestomSlotToWindowSlot(slot);
            if (openInventory != null) {
                slot = slot - 9 + offset;
            }
        }
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) slot, (byte) 0,
                shift ? ClientClickWindowPacket.ClickType.QUICK_MOVE : ClientClickWindowPacket.ClickType.PICKUP, Map.of(), ItemStack.Hash.AIR));
        player.interpretPacketQueue();
    }
}