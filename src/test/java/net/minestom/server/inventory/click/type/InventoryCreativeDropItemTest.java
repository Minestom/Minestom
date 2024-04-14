package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.DropFromPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.magic;

public class InventoryCreativeDropItemTest {

    static {
        MinecraftServer.init();
    }

    @Test
    public void testDropItem() {
        assertClick(
                List.of(),
                new Click.Info.CreativeDropItem(magic(64)),
                List.of(new DropFromPlayer(magic(64)))
        );

        // Make sure it doesn't drop a full stack
        assertClick(
                List.of(),
                new Click.Info.CreativeDropItem(magic(1)),
                List.of(new DropFromPlayer(magic(1)))
        );
    }

}
