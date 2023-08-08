package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryCreativeDropItemTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testDropItem() {
        assertClick(
                builder -> builder,
                new Click.Info.CreativeDropItem(ItemStack.of(Material.DIRT, 64)),
                builder -> builder.sideEffects(new Click.SideEffect.DropFromPlayer(ItemStack.of(Material.DIRT, 64)))
        );

        // Make sure it doesn't drop a full stack
        assertClick(
                builder -> builder,
                new Click.Info.CreativeDropItem(ItemStack.of(Material.DIRT, 1)),
                builder -> builder.sideEffects(new Click.SideEffect.DropFromPlayer(ItemStack.of(Material.DIRT, 1)))
        );
    }

}
