package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.Main;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryCreativeSetItemTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testSetItem() {
        assertClick(
                List.of(),
                new Click.Info.CreativeSetItem(0, magic(64)),
                List.of(new Main(0, magic(64)))
        );

        // Make sure it doesn't set a full stack
        assertClick(
                List.of(),
                new Click.Info.CreativeSetItem(0, magic(1)),
                List.of(new Main(0, magic(1)))
        );
    }

}
