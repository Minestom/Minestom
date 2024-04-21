package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.Container;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.*;

public class InventoryMiddleDragTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.MiddleDrag(IntList.of()), List.of());
    }

    @Test
    public void testExistingSlots() {
        assertClick(
                List.of(new Container(0, magic(1)), new Cursor(magic2(1))),
                new Click.Info.MiddleDrag(IntList.of(0)),
                List.of()
        );
    }

    @Test
    public void testPartialExistingSlots() {
        assertClick(
                List.of(new Container(0, magic(1)), new Cursor(magic2(1))),
                new Click.Info.MiddleDrag(IntList.of(0, 1)),
                List.of(new Container(1, magic2(1)))
        );
    }

    @Test
    public void testFullCopy() {
        assertClick(
                List.of(new Cursor(magic(1))),
                new Click.Info.MiddleDrag(IntList.of(0, 1)),
                List.of(new Container(0, magic(1)), new Container(1, magic(1)))
        );
    }

}
