package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.Main;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.*;

public class InventoryRightClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.Right(0), List.of());
    }

    @Test
    public void testAddOne() {
        assertClick(
                List.of(new Main(0, magic(32)), new Cursor(magic(32))),
                new Click.Info.Right(0),
                List.of(new Main(0, magic(33)), new Cursor(magic(31)))
        );
    }

    @Test
    public void testClickedStackFull() {
        assertClick(
                List.of(new Main(0, magic(64)), new Cursor(magic(32))),
                new Click.Info.Right(0),
                List.of()
        );
    }

    @Test
    public void testTakeHalf() {
        assertClick(
                List.of(new Main(0, magic(32))),
                new Click.Info.Right(0),
                List.of(new Main(0, magic(16)), new Cursor(magic(16)))
        );
    }

    @Test
    public void testLeaveOne() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.Right(0),
                List.of(new Main(0, magic(1)), new Cursor(magic(31)))
        );
    }

    @Test
    public void testSwitchItems() {
        assertClick(
                List.of(new Main(0, magic(1)), new Cursor(magic2(1))),
                new Click.Info.Right(0),
                List.of(new Main(0, magic2(1)), new Cursor(magic(1)))
        );
    }

}
