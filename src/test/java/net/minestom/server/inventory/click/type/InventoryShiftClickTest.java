package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.*;

public class InventoryShiftClickTest {

    static {
        MinecraftServer.init();
    }
    
    @Test
    public void testNoChanges() {
        assertClick(builder -> builder, new Click.Info.LeftShift(0), builder -> builder);
        assertClick(builder -> builder, new Click.Info.RightShift(0), builder -> builder);
    }

    @Test
    public void testSimpleTransfer() {
        assertClick(
                builder -> builder.setPlayer(9, ItemStack.of(Material.STONE, 32)),
                new Click.Info.LeftShift(SIZE),
                builder -> builder.set(0, ItemStack.of(Material.STONE, 32)).setPlayer(9, ItemStack.AIR)
        );
    }

    @Test
    public void testSeparatedTransfer() {
        assertClick(
                builder -> builder
                        .setPlayer(9, ItemStack.of(Material.STONE, 64))
                        .set(0, ItemStack.of(Material.STONE, 32))
                        .set(1, ItemStack.of(Material.STONE, 32))
                        ,
                new Click.Info.LeftShift(SIZE),
                builder -> builder
                        .setPlayer(9, ItemStack.AIR)
                        .set(0, ItemStack.of(Material.STONE, 64))
                        .set(1, ItemStack.of(Material.STONE, 64))
                        
        );
    }

    @Test
    public void testSeparatedAndNewTransfer() {
        assertClick(
                builder -> builder
                        .setPlayer(9, ItemStack.of(Material.STONE, 64))
                        .set(0, ItemStack.of(Material.STONE, 32)),
                new Click.Info.LeftShift(SIZE),
                builder -> builder
                        .setPlayer(9, ItemStack.AIR)
                        .set(0, ItemStack.of(Material.STONE, 64))
                        .set(1, ItemStack.of(Material.STONE, 32))
                        
        );
    }

    @Test
    public void testPartialTransfer() {
        assertClick(
                builder -> builder
                        .setPlayer(9, ItemStack.of(Material.STONE, 64))
                        .set(0, ItemStack.of(Material.STONE, 32))
                        .set(1, ItemStack.of(Material.DIRT))
                        .set(2, ItemStack.of(Material.DIRT))
                        .set(3, ItemStack.of(Material.DIRT))
                        .set(4, ItemStack.of(Material.DIRT)),
                new Click.Info.LeftShift(SIZE),
                builder -> builder
                        .setPlayer(9, ItemStack.of(Material.STONE, 32))
                        .set(0, ItemStack.of(Material.STONE, 64))
                        
        );
    }

    @Test
    public void testCannotTransfer() {
        assertClick(
                builder -> builder
                        .setPlayer(9, ItemStack.of(Material.STONE, 64))
                        .set(0, ItemStack.of(Material.STONE, 64))
                        .set(1, ItemStack.of(Material.DIRT))
                        .set(2, ItemStack.of(Material.DIRT))
                        .set(3, ItemStack.of(Material.DIRT))
                        .set(4, ItemStack.of(Material.DIRT)),
                new Click.Info.LeftShift(SIZE), // Equivalent to player slot 9
                builder -> builder
        );

        assertClick(
                builder -> builder
                        .setPlayer(9, ItemStack.of(Material.STONE, 64))
                        .set(0, ItemStack.of(Material.DIRT))
                        .set(1, ItemStack.of(Material.DIRT))
                        .set(2, ItemStack.of(Material.DIRT))
                        .set(3, ItemStack.of(Material.DIRT))
                        .set(4, ItemStack.of(Material.DIRT)),
                new Click.Info.LeftShift(SIZE), // Equivalent to player slot 9
                builder -> builder
        );
    }

    @Test
    public void testPlayerInteraction() {
        assertPlayerClick(
                builder -> builder.set(9, ItemStack.of(Material.STONE, 32)),
                new Click.Info.LeftShift(9),
                builder -> builder.set(9, ItemStack.AIR).set(0, ItemStack.of(Material.STONE, 32))
        );

        assertPlayerClick(
                builder -> builder.set(8, ItemStack.of(Material.STONE, 32)),
                new Click.Info.LeftShift(8),
                builder -> builder.set(8, ItemStack.AIR).set(9, ItemStack.of(Material.STONE, 32))
        );

        assertPlayerClick(
                builder -> builder.set(9, ItemStack.of(Material.IRON_CHESTPLATE)),
                new Click.Info.LeftShift(9),
                builder -> builder.set(9, ItemStack.AIR).set(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE))
        );

        assertPlayerClick(
                builder -> builder.set(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE)),
                new Click.Info.LeftShift(PlayerInventoryUtils.CHESTPLATE_SLOT),
                builder -> builder.set(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.AIR).set(9, ItemStack.of(Material.IRON_CHESTPLATE))
        );

        assertPlayerClick(
                builder -> builder.set(9, ItemStack.of(Material.SHIELD)),
                new Click.Info.LeftShift(9),
                builder -> builder.set(9, ItemStack.AIR).set(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.SHIELD))
        );

        assertPlayerClick(
                builder -> builder.set(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.SHIELD)),
                new Click.Info.LeftShift(PlayerInventoryUtils.OFF_HAND_SLOT),
                builder -> builder.set(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.AIR).set(9, ItemStack.of(Material.SHIELD))
        );
    }

}
