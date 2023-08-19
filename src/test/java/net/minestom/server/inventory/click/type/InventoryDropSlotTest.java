package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryDropSlotTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new ClickInfo.DropSlot(0, false), builder -> builder);
        assertClick(builder -> builder, new ClickInfo.DropSlot(0, true), builder -> builder);
    }

    @Test
    public void testDropEntireStack() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DropSlot(0, true),
                builder -> builder.change(0, ItemStack.AIR).sideEffects(new ClickResult.SideEffects.DropFromPlayer(ItemStack.of(Material.STONE, 32)))
        );
    }

    @Test
    public void testDropSingleItem() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.STONE, 32)),
                new ClickInfo.DropSlot(0, false),
                builder -> builder.change(0, ItemStack.of(Material.STONE, 31)).sideEffects(new ClickResult.SideEffects.DropFromPlayer(ItemStack.of(Material.STONE, 1)))
        );
    }

}
