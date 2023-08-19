package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;

public class InventoryCopyItemTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new ClickInfo.CopyItem(0), builder -> builder);
    }

    @Test
    public void testCopy() {
        assertClick(
                builder -> builder.change(0, ItemStack.of(Material.DIRT)),
                new ClickInfo.CopyItem(0),
                builder -> builder.cursor(ItemStack.of(Material.DIRT))
        );
    }

}
