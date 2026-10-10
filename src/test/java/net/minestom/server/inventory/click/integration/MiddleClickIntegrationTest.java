package net.minestom.server.inventory.click.integration;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryClickEvent;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class MiddleClickIntegrationTest {

    @Test
    public void cloneInOwnInventory(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.CREATIVE);
        var inventory = player.getInventory();
        inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 3));
        var listener = env.listen(InventoryClickEvent.class);
        listener.followup(event -> {
            assertEquals(inventory, event.getInventory());
            assertEquals(ClickType.MIDDLE_CLICK, event.getClickType());
            assertEquals(1, event.getSlot());
        });
        middleClick(player, 1);
        assertEquals(ItemStack.of(Material.DIAMOND, 64), inventory.getCursorItem());
        assertEquals(ItemStack.of(Material.DIAMOND, 3), inventory.getItemStack(1));
    }

    @Test
    public void cloneNeedsCreativeAndAnEmptyCursor(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = player.getInventory();
        inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 3));
        // Survival
        middleClick(player, 1);
        assertEquals(ItemStack.AIR, inventory.getCursorItem());
        // Cursor occupied
        player.setGameMode(GameMode.CREATIVE);
        inventory.setCursorItem(ItemStack.of(Material.STONE));
        middleClick(player, 1);
        assertEquals(ItemStack.of(Material.STONE), inventory.getCursorItem());
        assertEquals(ItemStack.of(Material.DIAMOND, 3), inventory.getItemStack(1));
    }

    @Test
    public void cloneInOpenInventory(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.CREATIVE);
        var inventory = new Inventory(InventoryType.CHEST_1_ROW, "Chest");
        inventory.setItemStack(0, ItemStack.of(Material.GOLD_INGOT, 5));
        player.openInventory(inventory);
        var listener = env.listen(InventoryClickEvent.class);
        listener.followup(event -> {
            assertEquals(inventory, event.getInventory());
            assertEquals(ClickType.MIDDLE_CLICK, event.getClickType());
        });
        middleClickOpenInventory(player, 0);
        assertEquals(ItemStack.of(Material.GOLD_INGOT, 64), player.getInventory().getCursorItem());
        assertEquals(ItemStack.of(Material.GOLD_INGOT, 5), inventory.getItemStack(0));
    }

    @Test
    public void cloneDragFillsEachSlot(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.CREATIVE);
        var inventory = new Inventory(InventoryType.CHEST_1_ROW, "Chest");
        inventory.setItemStack(2, ItemStack.of(Material.DIAMOND, 10));
        inventory.setItemStack(4, ItemStack.of(Material.STONE));
        player.openInventory(inventory);
        player.getInventory().setCursorItem(ItemStack.of(Material.DIAMOND));
        cloneDrag(player, inventory, List.of(1, 2, 3, 4));
        assertEquals(ItemStack.of(Material.DIAMOND, 64), inventory.getItemStack(1));
        assertEquals(ItemStack.of(Material.DIAMOND, 64), inventory.getItemStack(2));
        assertEquals(ItemStack.of(Material.DIAMOND, 64), inventory.getItemStack(3));
        assertEquals(ItemStack.of(Material.STONE), inventory.getItemStack(4));
        assertEquals(ItemStack.AIR, player.getInventory().getCursorItem());
    }

    @Test
    public void cloneDragNeedsCreative(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        var inventory = new Inventory(InventoryType.CHEST_1_ROW, "Chest");
        player.openInventory(inventory);
        player.getInventory().setCursorItem(ItemStack.of(Material.DIAMOND));
        cloneDrag(player, inventory, List.of(1, 2));
        assertEquals(ItemStack.AIR, inventory.getItemStack(1));
        assertEquals(ItemStack.of(Material.DIAMOND), player.getInventory().getCursorItem());
    }

    private static void middleClick(Player player, int slot) {
        final AbstractInventory open = player.getOpenInventory();
        final byte windowId = open != null ? open.getWindowId() : 0;
        int windowSlot = PlayerInventoryUtils.convertMinestomSlotToWindowSlot(slot);
        if (open != null) windowSlot = windowSlot - 9 + open.getInnerSize();
        click(player, windowId, ClientClickWindowPacket.ClickType.CLONE, 2, windowSlot);
    }

    private static void middleClickOpenInventory(Player player, int slot) {
        final AbstractInventory open = player.getOpenInventory();
        click(player, open.getWindowId(), ClientClickWindowPacket.ClickType.CLONE, 2, slot);
    }

    private static void cloneDrag(Player player, AbstractInventory open, List<Integer> slots) {
        // quick-craft buttons: 8 start, 9 per slot, 10 end
        click(player, open.getWindowId(), ClientClickWindowPacket.ClickType.QUICK_CRAFT, 8, -999);
        for (int slot : slots) click(player, open.getWindowId(), ClientClickWindowPacket.ClickType.QUICK_CRAFT, 9, slot);
        click(player, open.getWindowId(), ClientClickWindowPacket.ClickType.QUICK_CRAFT, 10, -999);
    }

    private static void click(Player player, byte windowId, ClientClickWindowPacket.ClickType type, int button, int slot) {
        player.addPacketToQueue(new ClientClickWindowPacket(windowId, 0, (short) slot, (byte) button, type, Map.of(), ItemStack.Hash.AIR));
        player.interpretPacketQueue();
    }
}
