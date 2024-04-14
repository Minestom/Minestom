package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Cursor;
import net.minestom.server.inventory.click.Click.Change.Main;
import net.minestom.server.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.*;

public class InventoryLeftClickTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.Left(0), List.of());
    }

    @Test
    public void testInsertEntireStack() {
        assertClick(
                List.of(new Main(0, magic(32)), new Click.Change.Cursor(magic(32))),
                new Click.Info.Left(0),
                List.of(new Main(0, magic(64)), new Cursor(ItemStack.AIR))
        );
    }

    @Test
    public void testInsertPartialStack() {
        assertClick(
                List.of(new Main(0, magic(32)), new Cursor(magic(48))),
                new Click.Info.Left(0),
                List.of(new Main(0, magic(64)), new Cursor(magic(16)))
        );
    }

    @Test
    public void testSwitchItems() {
        assertClick(
                List.of(new Main(0, magic(1)), new Cursor(magic2(1))),
                new Click.Info.Left(0),
                List.of(new Main(0, magic2(1)), new Cursor(magic(1)))
        );
    }

}
