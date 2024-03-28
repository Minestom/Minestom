package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryLeftClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new ClickInfo.LeftClick(0), builder -> builder);
    }

    @Test
    public void testInsertEntireStack() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 32)),
                new ClickInfo.LeftClick(0),
                builder -> builder.change(0, ItemStack.of(Material.STONE, 64)).cursor(ItemStack.AIR)
        );
    }

    @Test
    public void testInsertPartialStack() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 48)),
                new ClickInfo.LeftClick(0),
                builder -> builder.change(0, ItemStack.of(Material.STONE, 64)).cursor(ItemStack.of(Material.STONE, 16))
        );
    }

    @Test
    public void testSwitchItems() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)),
                new ClickInfo.LeftClick(0),
                builder -> builder.change(0, ItemStack.of(Material.DIRT)).cursor(ItemStack.of(Material.STONE))
        );
    }

}
