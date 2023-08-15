package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
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
        assertClick(ClickResult.empty(), new ClickInfo.CopyItem(0), ClickResult.empty());
    }

    @Test
    public void testCopy() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.DIRT)).build(),
                new ClickInfo.CopyItem(0),
                ClickResult.builder().cursor(ItemStack.of(Material.DIRT)).build()
        );
    }

}
