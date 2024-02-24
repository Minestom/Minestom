package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.click.ClickInfo;
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
        assertClick(builder -> builder, new ClickInfo.ShiftClick(0), builder -> builder);
    }

    @Test
    public void testSimpleTransfer() {
        assertClick(
                builder -> builder.change(9, ItemStack.of(Material.STONE, 32), true),
                new ClickInfo.ShiftClick(SIZE),
                builder -> builder.change(0, ItemStack.of(Material.STONE, 32)).change(9, ItemStack.AIR, true)
        );
    }

    @Test
    public void testSeparatedTransfer() {
        assertClick(
                builder -> builder
                        .change(9, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 32))
                        .change(1, ItemStack.of(Material.STONE, 32))
                        ,
                new ClickInfo.ShiftClick(SIZE),
                builder -> builder
                        .change(9, ItemStack.AIR, true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .change(1, ItemStack.of(Material.STONE, 64))
                        
        );
    }

    @Test
    public void testSeparatedAndNewTransfer() {
        assertClick(
                builder -> builder
                        .change(9, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 32)),
                new ClickInfo.ShiftClick(SIZE),
                builder -> builder
                        .change(9, ItemStack.AIR, true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .change(1, ItemStack.of(Material.STONE, 32))
                        
        );
    }

    @Test
    public void testPartialTransfer() {
        assertClick(
                builder -> builder
                        .change(9, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 32))
                        .change(1, ItemStack.of(Material.DIRT))
                        .change(2, ItemStack.of(Material.DIRT))
                        .change(3, ItemStack.of(Material.DIRT))
                        .change(4, ItemStack.of(Material.DIRT)),
                new ClickInfo.ShiftClick(SIZE),
                builder -> builder
                        .change(9, ItemStack.of(Material.STONE, 32), true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        
        );
    }

    @Test
    public void testCannotTransfer() {
        assertClick(
                builder -> builder
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .change(1, ItemStack.of(Material.DIRT))
                        .change(2, ItemStack.of(Material.DIRT))
                        .change(3, ItemStack.of(Material.DIRT))
                        .change(4, ItemStack.of(Material.DIRT)),
                new ClickInfo.ShiftClick(SIZE),
                builder -> builder
        );

        assertClick(
                builder -> builder
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.DIRT))
                        .change(1, ItemStack.of(Material.DIRT))
                        .change(2, ItemStack.of(Material.DIRT))
                        .change(3, ItemStack.of(Material.DIRT))
                        .change(4, ItemStack.of(Material.DIRT)),
                new ClickInfo.ShiftClick(SIZE),
                builder -> builder
        );
    }

    @Test
    public void testPlayerInteraction() {
        assertPlayerClick(
                builder -> builder.change(9, ItemStack.of(Material.STONE, 32)),
                new ClickInfo.ShiftClick(9),
                builder -> builder.change(9, ItemStack.AIR).change(0, ItemStack.of(Material.STONE, 32))
        );

        assertPlayerClick(
                builder -> builder.change(8, ItemStack.of(Material.STONE, 32)),
                new ClickInfo.ShiftClick(8),
                builder -> builder.change(8, ItemStack.AIR).change(9, ItemStack.of(Material.STONE, 32))
        );

        assertPlayerClick(
                builder -> builder.change(9, ItemStack.of(Material.IRON_CHESTPLATE)),
                new ClickInfo.ShiftClick(9),
                builder -> builder.change(9, ItemStack.AIR).change(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE))
        );

        assertPlayerClick(
                builder -> builder.change(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE)),
                new ClickInfo.ShiftClick(PlayerInventoryUtils.CHESTPLATE_SLOT),
                builder -> builder.change(PlayerInventoryUtils.CHESTPLATE_SLOT, ItemStack.AIR).change(9, ItemStack.of(Material.IRON_CHESTPLATE))
        );

        assertPlayerClick(
                builder -> builder.change(9, ItemStack.of(Material.SHIELD)),
                new ClickInfo.ShiftClick(9),
                builder -> builder.change(9, ItemStack.AIR).change(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.SHIELD))
        );

        assertPlayerClick(
                builder -> builder.change(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.of(Material.SHIELD)),
                new ClickInfo.ShiftClick(PlayerInventoryUtils.OFF_HAND_SLOT),
                builder -> builder.change(PlayerInventoryUtils.OFF_HAND_SLOT, ItemStack.AIR).change(9, ItemStack.of(Material.SHIELD))
        );
    }

}
