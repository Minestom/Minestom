package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.Main;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryMiddleClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.Middle(0), List.of());
    }

    @Test
    public void testCopy() {
        assertClick(
                List.of(new Main(0, magic(64))),
                new Click.Info.Middle(0),
                List.of(new Cursor(magic(64)))
        );
    }

    @Test
    public void testCopyNotFull() {
        assertClick(
                List.of(new Main(0, magic(32))),
                new Click.Info.Middle(0),
                List.of(new Cursor(magic(64)))
        );
    }

}
