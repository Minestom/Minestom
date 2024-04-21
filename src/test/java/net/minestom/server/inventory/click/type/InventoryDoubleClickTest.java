package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.Container;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryDoubleClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.Double(0), List.of());
    }

    @Test
    public void testCannotTakeAny() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.Double(0),
                List.of()
        );
    }

    @Test
    public void testPartialTake() {
        assertClick(
                List.of(new Container(1, magic(48)), new Cursor(magic(32))),
                new Click.Info.Double(0),
                List.of(new Container(1, magic(16)), new Cursor(magic(64)))
        );
    }

    @Test
    public void testTakeAll() {
        assertClick(
                List.of(new Container(1, magic(32)), new Cursor(magic(32))),
                new Click.Info.Double(0),
                List.of(new Container(1, ItemStack.AIR), new Cursor(magic(64)))
        );

        assertClick(
                List.of(new Container(1, magic(16)), new Cursor(magic(32))),
                new Click.Info.Double(0),
                List.of(new Container(1, ItemStack.AIR), new Cursor(magic(48)))
        );
    }

    @Test
    public void testTakeSeparated() {
        assertClick(
                List.of(new Container(1, magic(16)), new Container(2, magic(16)), new Cursor(magic(32))),
                new Click.Info.Double(0),
                List.of(new Container(1, ItemStack.AIR), new Container(2, ItemStack.AIR), new Cursor(magic(64)))
        );

        assertClick(
                List.of(new Container(1, magic(16)), new Container(2, magic(32)), new Cursor(magic(32))),
                new Click.Info.Double(0),
                List.of(new Container(1, ItemStack.AIR), new Container(2, magic(16)), new Cursor(magic(64)))
        );
    }

    @Test
    public void testCursorFull() {
        assertClick(
                List.of(new Container(1, magic(48)), new Cursor(magic(64))),
                new Click.Info.Double(0),
                List.of()
        );
    }

}
