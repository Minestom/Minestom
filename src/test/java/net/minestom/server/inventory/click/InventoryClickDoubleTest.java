package net.minestom.server.inventory.click;

import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class InventoryClickDoubleTest extends ClickUtils {

    static {
        // Required for now
        MinecraftServer.init();
    }

    @Test
    public void empty() {
        assertDouble((playerInventory, inventory) -> ClickProcessor.doubleClick(playerInventory, inventory, ItemStack.AIR),
                ItemStack.AIR, Map.of(), Map.of());
    }

    @Test
    public void takeOne() {
        assertDouble((playerInventory, inventory) -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND));
            return ClickProcessor.doubleClick(playerInventory, inventory, ItemStack.of(Material.DIAMOND));
        }, ItemStack.of(Material.DIAMOND, 2), Map.of(), Map.of(0, ItemStack.AIR));
    }

    @Test
    public void order() {
        assertDouble((playerInventory, inventory) -> {
            inventory.setItemStack(0, ItemStack.of(Material.DIAMOND, 31));
            playerInventory.setItemStack(9, ItemStack.of(Material.DIAMOND, 33));
            return ClickProcessor.doubleClick(playerInventory, inventory, ItemStack.of(Material.DIAMOND, 32));
        }, ItemStack.of(Material.DIAMOND, 64), Map.of(9, ItemStack.of(Material.DIAMOND, 32)), Map.of(0, ItemStack.AIR));
    }
}
