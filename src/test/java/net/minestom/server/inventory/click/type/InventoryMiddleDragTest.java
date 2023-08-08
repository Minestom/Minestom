package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryMiddleDragTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new Click.Info.MiddleDrag(IntList.of()), builder -> builder);
    }

    @Test
    public void testExistingSlots() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)),
                new Click.Info.MiddleDrag(IntList.of(0)),
                builder -> builder
        );
    }

    @Test
    public void testPartialExistingSlots() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)),
                new Click.Info.MiddleDrag(IntList.of(0, 1)),
                builder -> builder.set(1, ItemStack.of(Material.DIRT))
        );
    }

    @Test
    public void testFullCopy() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT)),
                new Click.Info.MiddleDrag(IntList.of(0, 1)),
                builder -> builder.set(0, ItemStack.of(Material.DIRT)).set(1, ItemStack.of(Material.DIRT))
        );
    }

}
