package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.DropFromPlayer;
import net.minestom.server.inventory.click.Click.Change.Main;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryDropSlotTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.DropSlot(0, false), List.of());
        assertClick(List.of(), new Click.Info.DropSlot(0, true), List.of());
    }

    @Test
    public void testDropEntireStack() {
        assertClick(
                List.of(new Main(0, magic(32))),
                new Click.Info.DropSlot(0, true),
                List.of(new Main(0, ItemStack.AIR), new DropFromPlayer(magic(32)))
        );
    }

    @Test
    public void testDropSingleItem() {
        assertClick(
                List.of(new Main(0, magic(32))),
                new Click.Info.DropSlot(0, false),
                List.of(new Main(0, magic(31)), new DropFromPlayer(magic(1)))
        );
    }

}
