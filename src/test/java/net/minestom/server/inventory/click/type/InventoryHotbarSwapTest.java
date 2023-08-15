package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryHotbarSwapTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        for (int i = 0; i < 9; i++) {
            assertClick(ClickResult.empty(), new ClickInfo.HotbarSwap(i, 0), ClickResult.empty());
        }
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).change(PlayerInventory.HOTBAR_START, ItemStack.of(Material.STONE), true).build(),
                new ClickInfo.HotbarSwap(0, 0),
                ClickResult.builder().change(0, ItemStack.of(Material.STONE)).change(PlayerInventory.HOTBAR_START, ItemStack.of(Material.DIRT), true).build()
        );
    }

}
