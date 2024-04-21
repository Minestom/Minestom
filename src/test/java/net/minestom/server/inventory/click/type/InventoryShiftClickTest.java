package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.Click.Change.*;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.minestom.server.inventory.click.ClickUtils.*;

public class InventoryShiftClickTest {

    static {
        MinecraftServer.init();
    }
    
    @Test
    public void testNoChanges() {
        assertClick(List.of(), new Click.Info.LeftShift(0), List.of());
        assertClick(List.of(), new Click.Info.RightShift(0), List.of());
    }

    @Test
    public void testSimpleTransfer() {
        assertClick(
                List.of(new Player(9, magic(32))),
                new Click.Info.LeftShift(SIZE),
                List.of(new Container(0, magic(32)), new Player(9, ItemStack.AIR))
        );
    }

    @Test
    public void testSeparatedTransfer() {
        assertClick(
                List.of(
                        new Player(9, magic(64)),
                        new Container(0, magic(32)),
                        new Container(1, magic(32))
                ),
                new Click.Info.LeftShift(SIZE),
                List.of(
                        new Player(9, ItemStack.AIR),
                        new Container(0, magic(64)),
                        new Container(1, magic(64))
                )
        );
    }

    @Test
    public void testSeparatedAndNewTransfer() {
        assertClick(
                List.of(
                        new Player(9, magic(64)),
                        new Container(0, magic(32))
                ),
                new Click.Info.LeftShift(SIZE),
                List.of(
                        new Player(9, ItemStack.AIR),
                        new Container(0, magic(64)),
                        new Container(1, magic(32))
                )
        );
    }

    @Test
    public void testPartialTransfer() {
        assertClick(
                List.of(
                        new Player(9, magic(64)),
                        new Container(0, magic(32)),
                        new Container(1, magic2(1)),
                        new Container(2, magic2(1)),
                        new Container(3, magic2(1)),
                        new Container(4, magic2(1))
                ),
                new Click.Info.LeftShift(SIZE),
                List.of(new Player(9, magic(32)), new Container(0, magic(64)))
        );
    }

    @Test
    public void testCannotTransfer() {
        assertClick(
                List.of(
                        new Player(9, magic(64)),
                        new Container(0, magic(64)),
                        new Container(1, magic2(1)),
                        new Container(2, magic2(1)),
                        new Container(3, magic2(1)),
                        new Container(4, magic2(1))
                ),
                new Click.Info.LeftShift(SIZE), // Equivalent to player slot 9
                List.of()
        );

        assertClick(
                List.of(
                        new Player(9, magic(64)),
                        new Container(0, magic2(1)),
                        new Container(1, magic2(1)),
                        new Container(2, magic2(1)),
                        new Container(3, magic2(1)),
                        new Container(4, magic2(1))
                ),
                new Click.Info.LeftShift(SIZE), // Equivalent to player slot 9
                List.of()
        );
    }

    @Test
    public void testPlayerInteraction() {
        assertPlayerClick(
                List.of(new Container(9, magic(32))),
                new Click.Info.LeftShift(9),
                List.of(new Container(9, ItemStack.AIR), new Container(0, magic(32)))
        );

        assertPlayerClick(
                List.of(new Container(8, magic(32))),
                new Click.Info.LeftShift(8),
                List.of(new Container(8, ItemStack.AIR), new Container(9, magic(32)))
        );

        assertPlayerClick(
                List.of(new Container(9, ItemStack.of(Material.IRON_CHESTPLATE))),
                new Click.Info.LeftShift(9),
                List.of(new Container(9, ItemStack.AIR), new Container(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE)))
        );

        assertPlayerClick(
                List.of(new Container(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE))),
                new Click.Info.LeftShift(PlayerInventoryUtils.CHESTPLATE_SLOT),
                List.of(new Container(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.AIR), new Container(9, ItemStack.of(Material.IRON_CHESTPLATE)))
        );

        assertPlayerClick(
                List.of(new Container(9, ItemStack.of(Material.SHIELD))),
                new Click.Info.LeftShift(9),
                List.of(new Container(9, ItemStack.AIR), new Container(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.SHIELD)))
        );

        assertPlayerClick(
                List.of(new Container(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.SHIELD))),
                new Click.Info.LeftShift(PlayerInventoryUtils.OFF_HAND_SLOT),
                List.of(new Container(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.AIR), new Container(9, ItemStack.of(Material.SHIELD)))
        );
    }

}
