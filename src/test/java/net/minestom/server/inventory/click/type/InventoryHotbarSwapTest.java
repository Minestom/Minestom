package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
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
            assertClick(builder -> builder, new ClickInfo.HotbarSwap(i, 9), builder -> builder);
        }
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.DIRT)).change(0, ItemStack.of(Material.STONE), true),
                new ClickInfo.HotbarSwap(0, 0),
                builder -> builder.change(0, ItemStack.of(Material.STONE)).change(0, ItemStack.of(Material.DIRT), true)
        );
    }

}
