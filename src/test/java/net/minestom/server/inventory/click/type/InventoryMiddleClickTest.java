package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryMiddleClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new Click.Info.Middle(0), builder -> builder);
    }

    @Test
    public void testCopy() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 64)),
                new Click.Info.Middle(0),
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 64))
        );
    }

    @Test
    public void testCopyNotFull() {
        assertClick(
                builder -> builder.set(0, ItemStack.of(Material.DIRT, 32)),
                new Click.Info.Middle(0),
                builder -> builder.cursor(ItemStack.of(Material.DIRT, 64))
        );
    }

}
