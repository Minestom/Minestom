package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryDropCursorTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(ClickResult.empty(), new ClickInfo.DropCursor(false), ClickResult.empty());
        assertClick(ClickResult.empty(), new ClickInfo.DropCursor(true), ClickResult.empty());
    }

    @Test
    public void testDropEntireStack() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.DropCursor(true),
                ClickResult.builder().cursor(ItemStack.AIR).sideEffects(new ClickResult.SideEffects.DropFromPlayer(ItemStack.of(Material.STONE, 32))).build()
        );
    }

    @Test
    public void testDropSingleItem() {
        assertClick(
                ClickResult.builder().cursor(ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.DropCursor(false),
                ClickResult.builder().cursor(ItemStack.of(Material.STONE, 31)).sideEffects(new ClickResult.SideEffects.DropFromPlayer(ItemStack.of(Material.STONE, 1))).build()
        );
    }

}
