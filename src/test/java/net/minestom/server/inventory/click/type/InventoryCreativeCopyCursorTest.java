package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryCreativeCopyCursorTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new ClickInfo.CreativeCopyCursor(IntList.of()), builder -> builder);
    }

    @Test
    public void testExistingSlots() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)),
                new ClickInfo.CreativeCopyCursor(IntList.of(0)),
                builder -> builder
        );
    }

    @Test
    public void testPartialExistingSlots() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)),
                new ClickInfo.CreativeCopyCursor(IntList.of(0, 1)),
                builder -> builder.change(1, ItemStack.of(Material.DIRT))
        );
    }

    @Test
    public void testFullCopy() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT)),
                new ClickInfo.CreativeCopyCursor(IntList.of(0, 1)),
                builder -> builder.change(0, ItemStack.of(Material.DIRT)).change(1, ItemStack.of(Material.DIRT))
        );
    }

}
