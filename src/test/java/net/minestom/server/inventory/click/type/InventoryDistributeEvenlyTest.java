package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryDistributeEvenlyTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoCursor() {
        assertClick(ClickResult.empty(), new ClickInfo.DistributeCursor(IntList.of(0), true), ClickResult.empty());
    }

    @Test
    public void testDistributeNone() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(), true),
                ClickResult.empty()
        );
    }

    @Test
    public void testDistributeOne() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0), true),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 32)).cursor(ItemStack.of(Material.AIR)).build()
        );
    }

    @Test
    public void testDistributeExactlyEnough() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0, 1), true),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 16)).change(1, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.AIR)).build()
        );

        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 30)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0, 1, 2), true),
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.DIRT, 10))
                        .change(1, ItemStack.of(Material.DIRT, 10))
                        .change(2, ItemStack.of(Material.DIRT, 10))
                        .cursor(ItemStack.of(Material.AIR)).build()
        );
    }

    @Test
    public void testRemainderItems() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0, 1, 2), true),
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.DIRT, 10))
                        .change(1, ItemStack.of(Material.DIRT, 10))
                        .change(2, ItemStack.of(Material.DIRT, 10))
                        .cursor(ItemStack.of(Material.DIRT, 2)).build()
        );

        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 25)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0, 1, 2, 3), true),
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.DIRT, 6))
                        .change(1, ItemStack.of(Material.DIRT, 6))
                        .change(2, ItemStack.of(Material.DIRT, 6))
                        .change(3, ItemStack.of(Material.DIRT, 6))
                        .cursor(ItemStack.of(Material.DIRT, 1)).build()
        );
    }

    @Test
    public void testDistributeOverExisting() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0), true),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 48)).cursor(ItemStack.of(Material.AIR)).build()
        );
    }

    @Test
    public void testDistributeOverFull() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 64)).cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0), true),
                ClickResult.empty()
        );
    }

}
