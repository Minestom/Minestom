package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryOffhandSwapTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new ClickInfo.OffhandSwap(0), builder -> builder);
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.DIRT)).change(PlayerInventory.OFF_HAND_SLOT, ItemStack.of(Material.STONE), true),
                new ClickInfo.OffhandSwap(0),
                builder -> builder.change(0, ItemStack.of(Material.STONE)).change(PlayerInventory.OFF_HAND_SLOT, ItemStack.of(Material.DIRT), true)
        );
    }

}
