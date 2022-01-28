package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class InventoryClickDragLeftPlayerTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertPlayerSingleClick(inventory -> ClickProcessor.leftDragWithinPlayer(inventory, ItemStack.AIR, List.of()),
                ItemStack.AIR, Map.of());
    }

    @Test
    public void dragStack() {
        assertPlayerSingleClick(inventory -> ClickProcessor.leftDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 64), List.of(0, 1)),
                ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND, 32), 1, ItemStack.of(Material.DIAMOND, 32)));
    }

    @Test
    public void dragTooMuch() {
        assertPlayerSingleClick(inventory -> ClickProcessor.leftDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 2), List.of(0, 1, 2)),
                ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND), 1, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void override() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 1));
            return ClickProcessor.leftDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 64), List.of(0, 1, 2));
        }, ItemStack.of(Material.DIAMOND), Map.of(0, ItemStack.of(Material.DIAMOND, 21), 1, ItemStack.of(Material.DIAMOND, 22), 2, ItemStack.of(Material.DIAMOND, 21)));
    }

    @Test
    public void full() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 64));
            return ClickProcessor.leftDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 64), List.of(0, 1, 2));
        }, ItemStack.of(Material.DIAMOND, 22), Map.of(0, ItemStack.of(Material.DIAMOND, 21), 2, ItemStack.of(Material.DIAMOND, 21)));
    }
}
