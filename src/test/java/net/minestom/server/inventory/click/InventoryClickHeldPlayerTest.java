package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickHeldPlayerTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertPlayerSingleClick(inventory -> ClickProcessor.held(inventory, inventory, 0, ItemStack.AIR, 0, ItemStack.AIR),
                ItemStack.AIR, Map.of());
    }

    @Test
    public void retrieve() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.held(inventory, inventory, 1, ItemStack.AIR, 0, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND), Map.of(0, ItemStack.AIR));
    }

    @Test
    public void swap() {
        assertPlayerSingleClick(inventory -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND));
            inventory.setItemStack(1, ItemStack.of(Material.GOLD_INGOT));
            return ClickProcessor.held(inventory, inventory, 1, ItemStack.of(Material.GOLD_INGOT), 0, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND), Map.of(0, ItemStack.of(Material.GOLD_INGOT)));
    }
}
