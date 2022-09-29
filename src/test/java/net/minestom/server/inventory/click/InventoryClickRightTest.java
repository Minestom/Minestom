package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickRightTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertSingleClick(inventory -> ClickProcessor.right(0, ItemStack.AIR, ItemStack.AIR),
                ItemStack.AIR, Map.of());
    }

    @Test
    public void insert() {
        assertSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.AIR);
            return ClickProcessor.right(0, ItemStack.AIR, ItemStack.of(Material.DIAMOND, 5));
        }, ItemStack.of(Material.DIAMOND, 4), Map.of(0, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void insertSingle() {
        assertSingleClick(inventory -> ClickProcessor.right(0, ItemStack.AIR, ItemStack.of(Material.DIAMOND)),
                ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void append() {
        assertSingleClick(inventory -> {
            var item = ItemStack.of(Material.DIAMOND, 2);
            inventory.setItemStack(0, item);
            return ClickProcessor.right(0, item, ItemStack.of(Material.DIAMOND, 5));
        }, ItemStack.of(Material.DIAMOND, 4), Map.of(0, ItemStack.of(Material.DIAMOND, 3)));
    }

    @Test
    public void take() {
        assertSingleClick(inventory -> {
            var item = ItemStack.of(Material.DIAMOND, 32);
            inventory.setItemStack(0, item);
            return ClickProcessor.right(0, item, ItemStack.AIR);
        }, ItemStack.of(Material.DIAMOND, 16), Map.of(0, ItemStack.of(Material.DIAMOND, 16)));
    }

    @Test
    public void takeSingle() {
        assertSingleClick(inventory -> {
            var item = ItemStack.of(Material.DIAMOND);
            inventory.setItemStack(0, item);
            return ClickProcessor.right(0, item, ItemStack.AIR);
        }, ItemStack.of(Material.DIAMOND), Map.of(0, ItemStack.AIR));
    }

    @Test
    public void takeOneOff() {
        assertSingleClick(inventory -> {
            var item = ItemStack.of(Material.DIAMOND, 33);
            inventory.setItemStack(0, item);
            return ClickProcessor.right(0, item, ItemStack.AIR);
        }, ItemStack.of(Material.DIAMOND, 17), Map.of(0, ItemStack.of(Material.DIAMOND, 16)));
    }

    @Test
    public void appendFullSlot() {
        assertSingleClick(inventory -> {
            var item = ItemStack.of(Material.DIAMOND, 64);
            inventory.setItemStack(0, item);
            return ClickProcessor.right(0, item, ItemStack.of(Material.DIAMOND, 5));
        }, ItemStack.of(Material.DIAMOND, 5), Map.of());
    }

    @Test
    public void swap() {
        var clicked = ItemStack.of(Material.STONE, 64);
        var cursor = ItemStack.of(Material.DIAMOND, 64);
        assertSingleClick(inventory -> {
            inventory.setItemStack(0, clicked);
            return ClickProcessor.right(0, clicked, cursor);
        }, clicked, Map.of(0, cursor));
    }
}
