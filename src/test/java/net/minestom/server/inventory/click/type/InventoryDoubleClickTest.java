package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryDoubleClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(ClickResult.empty(), new ClickInfo.DoubleClick(0), ClickResult.empty());
    }

    @Test
    public void testCannotTakeAny() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.empty()
        );
    }

    @Test
    public void testPartialTake() {
        assertClick(
                ClickResult.builder().change(1, ItemStack.of(Material.STONE, 48)).cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.builder().change(1, ItemStack.of(Material.STONE, 16)).cursor(ItemStack.of(Material.STONE, 64)).build()
        );
    }

    @Test
    public void testTakeAll() {
        assertClick(
                ClickResult.builder().change(1, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.builder().change(1, ItemStack.AIR).cursor(ItemStack.of(Material.STONE, 64)).build()
        );

        assertClick(
                ClickResult.builder().change(1, ItemStack.of(Material.STONE, 16)).cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.builder().change(1, ItemStack.AIR).cursor(ItemStack.of(Material.STONE, 48)).build()
        );
    }

    @Test
    public void testTakeSeparated() {
        assertClick(
                ClickResult.builder()
                        .change(1, ItemStack.of(Material.STONE, 16))
                        .change(2, ItemStack.of(Material.STONE, 16))
                        .cursor(ItemStack.of(Material.STONE, 32))
                        .build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.builder()
                        .change(1, ItemStack.AIR)
                        .change(2, ItemStack.AIR)
                        .cursor(ItemStack.of(Material.STONE, 64))
                        .build()
        );

        assertClick(
                ClickResult.builder()
                        .change(1, ItemStack.of(Material.STONE, 16))
                        .change(2, ItemStack.of(Material.STONE, 32))
                        .cursor(ItemStack.of(Material.STONE, 32))
                        .build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.builder()
                        .change(1, ItemStack.AIR)
                        .change(2, ItemStack.of(Material.STONE, 16))
                        .cursor(ItemStack.of(Material.STONE, 64))
                        .build()
        );
    }

    @Test
    public void testCursorFull() {
        assertClick(
                ClickResult.builder().change(1, ItemStack.of(Material.STONE, 48)).cursor(ItemStack.of(Material.STONE, 64)).build(),
                new ClickInfo.DoubleClick(0),
                ClickResult.empty()
        );
    }

}
