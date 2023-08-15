package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
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
        assertClick(ClickResult.empty(), new ClickInfo.LeftClick(0), ClickResult.empty());
    }

    @Test
    public void testInsertEntireStack() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.LeftClick(0),
                ClickResult.builder().change(0, ItemStack.of(Material.STONE, 64)).cursor(ItemStack.AIR).build()
        );
    }

    @Test
    public void testInsertPartialStack() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.STONE, 32)).cursor(ItemStack.of(Material.STONE, 48)).build(),
                new ClickInfo.LeftClick(0),
                ClickResult.builder().change(0, ItemStack.of(Material.STONE, 64)).cursor(ItemStack.of(Material.STONE, 16)).build()
        );
    }

    @Test
    public void testSwitchItems() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.STONE)).cursor(ItemStack.of(Material.DIRT)).build(),
                new ClickInfo.LeftClick(0),
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).cursor(ItemStack.of(Material.STONE)).build()
        );
    }

}
