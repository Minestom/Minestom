package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
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
        assertClick(builder -> builder, new ClickInfo.DoubleClick(0), builder -> builder);
    }

    @Test
    public void testCannotTakeAny() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DoubleClick(0),
                builder -> builder
        );
    }

    @Test
    public void testPartialTake() {
        assertClick(
                builder -> builder.change(1, ItemStack.of(Material.STONE, 48)).cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DoubleClick(0),
                builder -> builder.change(1, ItemStack.of(Material.STONE, 16)).cursor(ItemStack.of(Material.STONE, 64))
        );
    }

    @Test
    public void testTakeAll() {
        assertClick(
                builder -> builder.change(1, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DoubleClick(0),
                builder -> builder.change(1, ItemStack.AIR).cursor(ItemStack.of(Material.STONE, 64))
        );

        assertClick(
                builder -> builder.change(1, ItemStack.of(Material.STONE, 16)).cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DoubleClick(0),
                builder -> builder.change(1, ItemStack.AIR).cursor(ItemStack.of(Material.STONE, 48))
        );
    }

    @Test
    public void testTakeSeparated() {
        assertClick(
                builder -> builder
                        .change(1, ItemStack.of(Material.STONE, 16))
                        .change(2, ItemStack.of(Material.STONE, 16))
                        .cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DoubleClick(0),
                builder -> builder
                        .change(1, ItemStack.AIR)
                        .change(2, ItemStack.AIR)
                        .cursor(ItemStack.of(Material.STONE, 64))
        );

        assertClick(
                builder -> builder
                        .change(1, ItemStack.of(Material.STONE, 16))
                        .change(2, ItemStack.of(Material.STONE, 32))
                        .cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DoubleClick(0),
                builder -> builder
                        .change(1, ItemStack.AIR)
                        .change(2, ItemStack.of(Material.STONE, 16))
                        .cursor(ItemStack.of(Material.STONE, 64))
        );
    }

    @Test
    public void testCursorFull() {
        assertClick(
                builder -> builder.change(1, ItemStack.of(Material.STONE, 48)).cursor(ItemStack.of(Material.STONE, 64)),
                new ClickInfo.DoubleClick(0),
                builder -> builder
        );
    }

}
