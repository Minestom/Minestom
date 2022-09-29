package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class InventoryClickDragRightPlayerTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertPlayerSingleClick(inventory -> ClickProcessor.rightDragWithinPlayer(inventory, ItemStack.AIR, List.of()),
                ItemStack.AIR, Map.of());
    }

    @Test
    public void dragStack() {
        assertPlayerSingleClick(inventory -> ClickProcessor.rightDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 64), List.of(0, 1)),
                ItemStack.of(Material.DIAMOND, 62), Map.of(0, ItemStack.of(Material.DIAMOND), 1, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void dragTooMuch() {
        assertPlayerSingleClick(inventory -> ClickProcessor.rightDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 2), List.of(0, 1, 2)),
                ItemStack.AIR, Map.of(0, ItemStack.of(Material.DIAMOND), 1, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void override() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 1));
            return ClickProcessor.rightDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 64), List.of(0, 1, 2));
        }, ItemStack.of(Material.DIAMOND, 61), Map.of(0, ItemStack.of(Material.DIAMOND), 1, ItemStack.of(Material.DIAMOND, 2), 2, ItemStack.of(Material.DIAMOND)));
    }

    @Test
    public void full() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(1, ItemStack.of(Material.DIAMOND, 64));
            return ClickProcessor.rightDragWithinPlayer(inventory, ItemStack.of(Material.DIAMOND, 64), List.of(0, 1, 2));
        }, ItemStack.of(Material.DIAMOND, 62), Map.of(0, ItemStack.of(Material.DIAMOND), 2, ItemStack.of(Material.DIAMOND)));
    }
}
