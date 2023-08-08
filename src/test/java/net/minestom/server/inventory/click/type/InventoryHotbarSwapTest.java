package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
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
            assertClick(builder -> builder, new Click.Info.HotbarSwap(i, 9), builder -> builder);
        }
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.DIRT)).setPlayer(0, ItemStack.of(Material.STONE)),
                new Click.Info.HotbarSwap(0, 0),
                builder -> builder.set(0, ItemStack.of(Material.STONE)).setPlayer(0, ItemStack.of(Material.DIRT))
        );
    }

}
