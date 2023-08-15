package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryDistributeOneTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoCursor() {
        assertClick(ClickResult.empty(), new ClickInfo.DistributeCursor(IntList.of(0), false), ClickResult.empty());
    }

    @Test
    public void testDistributeNone() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(), false),
                ClickResult.empty()
        );
    }

    @Test
    public void testDistributeOne() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0), false),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).cursor(ItemStack.of(Material.DIRT, 31)).build()
        );
    }

    @Test
    public void testDistributeExactlyEnough() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 2)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0, 1), false),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).change(1, ItemStack.of(Material.DIRT)).cursor(ItemStack.of(Material.AIR)).build()
        );
    }

    @Test
    public void testTooManySlots() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT, 2)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0, 1, 2), false),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).change(1, ItemStack.of(Material.DIRT)).cursor(ItemStack.of(Material.AIR)).build()
        );
    }

    @Test
    public void testDistributeOverExisting() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 16)).cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0), false),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 17)).cursor(ItemStack.of(Material.DIRT, 31)).build()
        );
    }

    @Test
    public void testDistributeOverFull() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT, 64)).cursor(ItemStack.of(Material.DIRT, 32)).build(),
                new ClickInfo.DistributeCursor(IntList.of(0), false),
                ClickResult.empty()
        );
    }

}
