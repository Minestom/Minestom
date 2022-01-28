package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickShiftTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertSingleClick(inventory -> ClickProcessor.shiftToInventory(inventory, ItemStack.of(Material.AIR)), ItemStack.AIR, Map.of());
    }

    @Test
    public void insertOne() {
        assertSingleClick(inventory -> ClickProcessor.shiftToInventory(inventory, ItemStack.of(Material.DIAMOND)), ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void incrOne() {
        assertSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.shiftToInventory(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND, 2)));
    }

    @Test
    public void overflow() {
        assertSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND, 32));
            return ClickProcessor.shiftToInventory(inventory, ItemStack.of(Material.DIAMOND, 33));
        }, ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND, 64),
                1, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void full() {
        assertSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND, 64));
            inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 64));
            inventory.setItemStack(2, ItemStack.of(Material.DIAMOND, 64));
            inventory.setItemStack(3, ItemStack.of(Material.DIAMOND, 64));
            inventory.setItemStack(4, ItemStack.of(Material.DIAMOND, 64));
            return ClickProcessor.shiftToInventory(inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND), Map.of());
    }
}
