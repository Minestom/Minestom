package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryLeftDragTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoCursor() {
        assertClick(builder -> builder, new Click.Info.LeftDrag(IntList.of(0)), builder -> builder);
    }

    @Test
    public void testDistributeNone() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new Click.Info.LeftDrag(IntList.of()),
                builder -> builder
        );
    }

    @Test
    public void testDistributeOne() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new Click.Info.LeftDrag(IntList.of(0)),
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 32)).cursor(ItemStack.of(Material.AIR))
        );
    }

    @Test
    public void testDistributeExactlyEnough() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new Click.Info.LeftDrag(IntList.of(0, 1)),
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 16)).set(1, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.AIR))
        );

        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 30)),
                new Click.Info.LeftDrag(IntList.of(0, 1, 2)),
                builder -> builder
                        .set(0, ItemStack.of(Material.DIRT, 10))
                        .set(1, ItemStack.of(Material.DIRT, 10))
                        .set(2, ItemStack.of(Material.DIRT, 10))
                        .cursor(ItemStack.of(Material.AIR))
        );
    }

    @Test
    public void testRemainderItems() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 32)),
                new Click.Info.LeftDrag(IntList.of(0, 1, 2)),
                builder -> builder
                        .set(0, ItemStack.of(Material.DIRT, 10))
                        .set(1, ItemStack.of(Material.DIRT, 10))
                        .set(2, ItemStack.of(Material.DIRT, 10))
                        .cursor(ItemStack.of(Material.DIRT, 2))
        );

        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 25)),
                new Click.Info.LeftDrag(IntList.of(0, 1, 2, 3)),
                builder -> builder
                        .set(0, ItemStack.of(Material.DIRT, 6))
                        .set(1, ItemStack.of(Material.DIRT, 6))
                        .set(2, ItemStack.of(Material.DIRT, 6))
                        .set(3, ItemStack.of(Material.DIRT, 6))
                        .cursor(ItemStack.of(Material.DIRT, 1))
        );
    }

    @Test
    public void testDistributeOverExisting() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.DIRT, 32)),
                new Click.Info.LeftDrag(IntList.of(0)),
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 48)).cursor(ItemStack.of(Material.AIR))
        );
    }

    @Test
    public void testDistributeOverFull() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 64)).cursor(ItemStack.of(Material.DIRT, 32)),
                new Click.Info.LeftDrag(IntList.of(0)),
                builder -> builder
        );
    }

}
