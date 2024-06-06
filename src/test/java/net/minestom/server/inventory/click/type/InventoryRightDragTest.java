package net.minestom.server.inventory.click.type;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.*;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryRightDragTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoCursor() {
        assertClick(List.of(), new Click.Info.RightDrag(IntList.of(0)), List.of());
    }

    @Test
    public void testDistributeNone() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.RightDrag(IntList.of()),
                List.of()
        );
    }

    @Test
    public void testDistributeOne() {
        assertClick(
                List.of(new Cursor(magic(32))),
                new Click.Info.RightDrag(IntList.of(0)),
                List.of(new Container(0, magic(1)), new Cursor(magic(31)))
        );
    }

    @Test
    public void testDistributeExactlyEnough() {
        assertClick(
                List.of(new Cursor(magic(2))),
                new Click.Info.RightDrag(IntList.of(0, 1)),
                List.of(new Container(0, magic(1)), new Container(1, magic(1)), new Cursor(ItemStack.AIR))
        );
    }

    @Test
    public void testTooManySlots() {
        assertClick(
                List.of(new Cursor(magic(2))),
                new Click.Info.RightDrag(IntList.of(0, 1, 2)),
                List.of(new Container(0, magic(1)), new Container(1, magic(1)), new Cursor(ItemStack.AIR))
        );
    }

    @Test
    public void testDistributeOverExisting() {
        assertClick(
                List.of(new Container(0, magic(16)), new Cursor(magic(32))),
                new Click.Info.RightDrag(IntList.of(0)),
                List.of(new Container(0, magic(17)), new Cursor(magic(31)))
        );
    }

    @Test
    public void testDistributeOverFull() {
        assertClick(
                List.of(new Container(0, magic(64)), new Cursor(magic(32))),
                new Click.Info.RightDrag(IntList.of(0)),
                List.of()
        );
    }

}
