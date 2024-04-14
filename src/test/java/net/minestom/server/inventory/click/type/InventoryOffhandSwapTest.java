package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Main;
import net.minestom.server.inventory.click.Click.Change.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.*;
import static net.minestom.server.utils.inventory.PlayerInventoryUtils.OFF_HAND_SLOT;

public class InventoryOffhandSwapTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.OffhandSwap(0), List.of());
    }

    @Test
    public void testSwappedItems() {
        assertClick(
                List.of(new Main(0, magic2(1)), new Player(OFF_HAND_SLOT, magic(1))),
                new Click.Info.OffhandSwap(0),
                List.of(new Main(0, magic(1)), new Player(OFF_HAND_SLOT, magic2(1)))
        );
    }

}
