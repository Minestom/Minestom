package net.minestom.server.inventory.click.integration;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@EnvTest
public class DragLeftClickIntegrationTest {

    @Test
    public void leftDragSelf(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = player.getInventory();
        var listener = env.listen(InventoryPreClickEvent.class);
        // Empty drag FIXME
        {
            //listener.failFollowup();
            //drag(player, List.of());
        }
        // Drag to air
        {
            inventory.setCursorItem(ItemStack.of(Material.STONE, 64));
            List<InventoryPreClickEvent> events = new ArrayList<>();
            listener.followup(events::add);
            dragLeft(player, List.of(1, 2));
            // start->slot->slot->end
            assertEquals(4, events.size());
            {
                // Start
                var event = events.get(0);
                assertEquals(ClickType.START_LEFT_DRAGGING, event.getClickType());
                assertEquals(-999, event.getSlot());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                assertEquals(ItemStack.AIR, event.getClickedItem());
            }
            {
                // Slot 1
                var event = events.get(1);
                assertEquals(ClickType.LEFT_DRAGGING, event.getClickType());
                assertEquals(1, event.getSlot());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                assertEquals(ItemStack.AIR, event.getClickedItem());
            }
            {
                // Slot 2
                var event = events.get(2);
                assertEquals(ClickType.LEFT_DRAGGING, event.getClickType());
                assertEquals(2, event.getSlot());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                assertEquals(ItemStack.AIR, event.getClickedItem());
            }
            {
                // End
                var event = events.get(3);
                assertEquals(ClickType.END_LEFT_DRAGGING, event.getClickType());
                assertEquals(2, event.getSlot());
                assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                assertEquals(ItemStack.AIR, event.getClickedItem());
            }

            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.of(Material.STONE, 32), inventory.getItemStack(1));
            assertEquals(ItemStack.of(Material.STONE, 32), inventory.getItemStack(2));
        }
        // Cancel start
        {
            inventory.clear();
            inventory.setCursorItem(ItemStack.of(Material.STONE, 64));
            listener.followup(event -> {
                if (event.getClickType() == ClickType.START_LEFT_DRAGGING) {
                    assertEquals(-999, event.getSlot());
                    assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                    assertEquals(ItemStack.AIR, event.getClickedItem());
                    event.setCancelled(true);
                } else {
                    fail("Start drag has been cancelled, following events should not be called");
                }
            });
            dragLeft(player, List.of(1, 2));
            assertEquals(ItemStack.of(Material.STONE, 64), inventory.getCursorItem());
            assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            assertEquals(ItemStack.AIR, inventory.getItemStack(2));
        }
        // Cancel end
        {
            inventory.clear();
            inventory.setCursorItem(ItemStack.of(Material.STONE, 64));
            listener.followup(event -> {
                if (event.getClickType() == ClickType.END_LEFT_DRAGGING) {
                    assertEquals(2, event.getSlot());
                    assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                    assertEquals(ItemStack.AIR, event.getClickedItem());
                    event.setCancelled(true);
                }
            });
            dragLeft(player, List.of(1, 2));
            assertEquals(ItemStack.of(Material.STONE, 64), inventory.getCursorItem());
            assertEquals(ItemStack.AIR, inventory.getItemStack(1));
            assertEquals(ItemStack.AIR, inventory.getItemStack(2));
        }
        // Cancel step
        {
            inventory.clear();
            inventory.setCursorItem(ItemStack.of(Material.STONE, 64));
            listener.followup(event -> {
                if (event.getClickType() == ClickType.LEFT_DRAGGING && event.getSlot() == 2) {
                    assertEquals(2, event.getSlot());
                    assertEquals(ItemStack.of(Material.STONE, 64), event.getCursorItem());
                    assertEquals(ItemStack.AIR, event.getClickedItem());
                    event.setCancelled(true);
                }
            });
            dragLeft(player, List.of(1, 2, 3));
            assertEquals(ItemStack.AIR, inventory.getCursorItem());
            assertEquals(ItemStack.of(Material.STONE, 32), inventory.getItemStack(1));
            assertEquals(ItemStack.AIR, inventory.getItemStack(2));
            assertEquals(ItemStack.of(Material.STONE, 32), inventory.getItemStack(3));
        }
    }

    private void dragLeftOpenInventory(Player player, List<Integer> slots) {
        _dragLeft(player.getOpenInventory(), true, player, slots);
    }

    private void dragLeft(Player player, List<Integer> slots) {
        _dragLeft(player.getOpenInventory(), false, player, slots);
    }

    private void _dragLeft(Inventory openInventory, boolean clickOpenInventory, Player player, List<Integer> slots) {
        final byte windowId = openInventory != null ? openInventory.getWindowId() : 0;

        // Start left drag
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) -999, (byte) 0,
                ClientClickWindowPacket.ClickType.QUICK_CRAFT, List.of(), ItemStack.AIR));
        // Add all slots
        for (int slot : slots) {
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
            player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) slot, (byte) 1,
                    ClientClickWindowPacket.ClickType.QUICK_CRAFT, List.of(), ItemStack.AIR));
        }
        // End left drag
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) -999, (byte) 2,
                ClientClickWindowPacket.ClickType.QUICK_CRAFT, List.of(), ItemStack.AIR));

        player.interpretPacketQueue();
    }
}
