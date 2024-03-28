package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryDragEvenlyTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoCursor() {
        assertClick(builder -> builder, new ClickInfo.DragClick(IntList.of(0), true), builder -> builder);
    }

    @Test
    public void testDistributeNone() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new ClickInfo.DragClick(IntList.of(), true),
                builder -> builder
        );
    }

    @Test
    public void testDistributeOne() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new ClickInfo.DragClick(IntList.of(0), true),
                builder -> builder.change(0, ItemStack.of(Material.DIRT, 32)).cursor(ItemStack.of(Material.AIR))
        );
    }

    @Test
    public void testDistributeExactlyEnough() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new ClickInfo.DragClick(IntList.of(0, 1), true),
                builder -> builder.change(0, ItemStack.of(Material.DIRT, 16)).change(1, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.AIR))
        );

        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 30)),
                new ClickInfo.DragClick(IntList.of(0, 1, 2), true),
                builder -> builder
                        .change(0, ItemStack.of(Material.DIRT, 10))
                        .change(1, ItemStack.of(Material.DIRT, 10))
                        .change(2, ItemStack.of(Material.DIRT, 10))
                        .cursor(ItemStack.of(Material.AIR))
        );
    }

    @Test
    public void testRemainderItems() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new ClickInfo.DragClick(IntList.of(0, 1, 2), true),
                builder -> builder
                        .change(0, ItemStack.of(Material.DIRT, 10))
                        .change(1, ItemStack.of(Material.DIRT, 10))
                        .change(2, ItemStack.of(Material.DIRT, 10))
                        .cursor(ItemStack.of(Material.DIRT, 2))
        );

        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 25)),
                new ClickInfo.DragClick(IntList.of(0, 1, 2, 3), true),
                builder -> builder
                        .change(0, ItemStack.of(Material.DIRT, 6))
                        .change(1, ItemStack.of(Material.DIRT, 6))
                        .change(2, ItemStack.of(Material.DIRT, 6))
                        .change(3, ItemStack.of(Material.DIRT, 6))
                        .cursor(ItemStack.of(Material.DIRT, 1))
        );
    }

    @Test
    public void testDistributeOverExisting() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.DIRT, 32)),
                new ClickInfo.DragClick(IntList.of(0), true),
                builder -> builder.change(0, ItemStack.of(Material.DIRT, 48)).cursor(ItemStack.of(Material.AIR))
        );
    }

    @Test
    public void testDistributeOverFull() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.DIRT, 64)).cursor(ItemStack.of(Material.DIRT, 32)),
                new ClickInfo.DragClick(IntList.of(0), true),
                builder -> builder
        );
    }

}
