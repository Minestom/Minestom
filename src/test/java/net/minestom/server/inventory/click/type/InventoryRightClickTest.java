package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryRightClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new Click.Info.Right(0), builder -> builder);
    }

    @Test
    public void testAddOne() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 32)),
                new Click.Info.Right(0),
                builder -> builder.set(0, ItemStack.of(Material.STONE, 33)).cursor(ItemStack.of(Material.STONE, 31))
        );
    }

    @Test
    public void testClickedStackFull() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.STONE, 64)).cursor(ItemStack.of(Material.STONE, 32)),
                new Click.Info.Right(0),
                builder -> builder
        );
    }

    @Test
    public void testTakeHalf() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.STONE, 32)),
                new Click.Info.Right(0),
                builder -> builder.set(0, ItemStack.of(Material.STONE, 16)).cursor(ItemStack.of(Material.STONE, 16))
        );
    }

    @Test
    public void testLeaveOne() {
        assertClick(
                builder -> builder.cursor(ItemStack.of(Material.STONE, 32)),
                new Click.Info.Right(0),
                builder -> builder.set(0, ItemStack.of(Material.STONE, 1)).cursor(ItemStack.of(Material.STONE, 31))
        );
    }

    @Test
    public void testSwitchItems() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)),
                new Click.Info.Right(0),
                builder -> builder.set(0, ItemStack.of(Material.DIRT)).cursor(ItemStack.of(Material.STONE))
        );
    }

}
