package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
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
        assertClick(ClickResult.empty(), new ClickInfo.OffhandSwap(0), ClickResult.empty());
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).change(PlayerInventory.OFFHAND_SLOT, ItemStack.of(Material.STONE), true).build(),
                new ClickInfo.OffhandSwap(0),
                ClickResult.builder().change(0, ItemStack.of(Material.STONE)).change(PlayerInventory.OFFHAND_SLOT, ItemStack.of(Material.DIRT), true).build()
        );
    }

}
