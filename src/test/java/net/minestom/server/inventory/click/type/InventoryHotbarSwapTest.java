package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Main;
import net.minestom.server.inventory.click.Click.Change.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.*;

public class InventoryHotbarSwapTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        for (int i = 0; i < 9; i++) {
            assertClick(List.of(), new Click.Info.HotbarSwap(i, 9), List.of());
        }
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                List.of(new Main(0, magic2(1)), new Player(0, magic(1))),
                new Click.Info.HotbarSwap(0, 0),
                List.of(new Main(0, magic(1)), new Player(0, magic2(1)))
        );
    }

}
