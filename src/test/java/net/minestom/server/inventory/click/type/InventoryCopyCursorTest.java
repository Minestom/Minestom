package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryCopyCursorTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(ClickResult.empty(), new ClickInfo.CopyCursor(IntList.of()), ClickResult.empty());
    }

    @Test
    public void testExistingSlots() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)).build(),
                new ClickInfo.CopyCursor(IntList.of(0)),
                ClickResult.empty()
        );
    }

    @Test
    public void testPartialExistingSlots() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)).build(),
                new ClickInfo.CopyCursor(IntList.of(0, 1)),
                ClickResult.builder().change(1, ItemStack.of(Material.DIRT)).build()
        );
    }

    @Test
    public void testFullCopy() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT)).build(),
                new ClickInfo.CopyCursor(IntList.of(0, 1)),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).change(1, ItemStack.of(Material.DIRT)).build()
        );
    }

}
