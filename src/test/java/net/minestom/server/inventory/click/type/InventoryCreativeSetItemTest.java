package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryCreativeSetItemTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testSetItem() {
        assertClick(
                builder -> builder,
                new Click.Info.CreativeSetItem(0, ItemStack.of(Material.DIRT, 64)),
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 64))
        );

        // Make sure it doesn't set a full stack
        assertClick(
                builder -> builder,
                new Click.Info.CreativeSetItem(0, ItemStack.of(Material.DIRT, 1)),
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 1))
        );
    }

}
