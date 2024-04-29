package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.DropFromPlayer;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryDropCursorTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.LeftDropCursor(), List.of());
        assertClick(List.of(), new Click.Info.MiddleDropCursor(), List.of());
        assertClick(List.of(), new Click.Info.RightDropCursor(), List.of());
    }

    @Test
    public void testDropEntireStack() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.LeftDropCursor(),
                List.of(new Cursor(ItemStack.AIR), new DropFromPlayer(magic(32)))
        );
    }

    @Test
    public void testDropSingleItem() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.RightDropCursor(),
                List.of(new Cursor(magic(31)), new DropFromPlayer(magic(1)))
        );
    }

    @Test
    public void testMiddleClickNoop() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.MiddleDropCursor(),
                List.of()
        );
    }

}
