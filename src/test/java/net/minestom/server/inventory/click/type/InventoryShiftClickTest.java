package net.minestom.server.inventory.click.type;

import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickPreprocessor;
import net.minestom.server.inventory.click.ClickResult;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static net.minestom.server.inventory.click.ClickUtils.assertClick;
import static net.minestom.server.inventory.click.ClickUtils.assertPlayerClick;

public class InventoryShiftClickTest {

    static {
        MinecraftServer.init();
    }
    
    @Test
    public void testNoChanges() {
        assertClick(ClickResult.empty(), new ClickInfo.ShiftClick(0), ClickResult.empty());
    }

    @Test
    public void testSimpleTransfer() {
        assertClick(
                ClickResult.builder().change(0, ItemStack.of(Material.STONE, 32), true).build(),
                new ClickInfo.ShiftClick(ClickPreprocessor.PLAYER_INVENTORY_OFFSET),
                ClickResult.builder().change(0, ItemStack.of(Material.STONE, 32)).change(0, ItemStack.AIR, true).build()
        );
    }

    @Test
    public void testSeparatedTransfer() {
        assertClick(
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 32))
                        .change(1, ItemStack.of(Material.STONE, 32))
                        .build(),
                new ClickInfo.ShiftClick(ClickPreprocessor.PLAYER_INVENTORY_OFFSET),
                ClickResult.builder()
                        .change(0, ItemStack.AIR, true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .change(1, ItemStack.of(Material.STONE, 64))
                        .build()
        );
    }

    @Test
    public void testSeparatedAndNewTransfer() {
        assertClick(
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 32))
                        .build(),
                new ClickInfo.ShiftClick(ClickPreprocessor.PLAYER_INVENTORY_OFFSET),
                ClickResult.builder()
                        .change(0, ItemStack.AIR, true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .change(1, ItemStack.of(Material.STONE, 32))
                        .build()
        );
    }

    @Test
    public void testPartialTransfer() {
        assertClick(
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 32))
                        .change(1, ItemStack.of(Material.DIRT))
                        .change(2, ItemStack.of(Material.DIRT))
                        .change(3, ItemStack.of(Material.DIRT))
                        .change(4, ItemStack.of(Material.DIRT))
                        .build(),
                new ClickInfo.ShiftClick(ClickPreprocessor.PLAYER_INVENTORY_OFFSET),
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.STONE, 32), true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .build()
        );
    }

    @Test
    public void testCannotTransfer() {
        assertClick(
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.STONE, 64))
                        .change(1, ItemStack.of(Material.DIRT))
                        .change(2, ItemStack.of(Material.DIRT))
                        .change(3, ItemStack.of(Material.DIRT))
                        .change(4, ItemStack.of(Material.DIRT))
                        .build(),
                new ClickInfo.ShiftClick(ClickPreprocessor.PLAYER_INVENTORY_OFFSET),
                ClickResult.empty()
        );

        assertClick(
                ClickResult.builder()
                        .change(0, ItemStack.of(Material.STONE, 64), true)
                        .change(0, ItemStack.of(Material.DIRT))
                        .change(1, ItemStack.of(Material.DIRT))
                        .change(2, ItemStack.of(Material.DIRT))
                        .change(3, ItemStack.of(Material.DIRT))
                        .change(4, ItemStack.of(Material.DIRT))
                        .build(),
                new ClickInfo.ShiftClick(ClickPreprocessor.PLAYER_INVENTORY_OFFSET),
                ClickResult.empty()
        );
    }

    @Test
    public void testPlayerInteraction() {
        assertPlayerClick(
                ClickResult.builder().change(9, ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.ShiftClick(9),
                ClickResult.builder().change(9, ItemStack.AIR).change(36, ItemStack.of(Material.STONE, 32)).build()
        );

        assertPlayerClick(
                ClickResult.builder().change(44, ItemStack.of(Material.STONE, 32)).build(),
                new ClickInfo.ShiftClick(44),
                ClickResult.builder().change(44, ItemStack.AIR).change(9, ItemStack.of(Material.STONE, 32)).build()
        );

        assertPlayerClick(
                ClickResult.builder().change(9, ItemStack.of(Material.IRON_CHESTPLATE)).build(),
                new ClickInfo.ShiftClick(9),
                ClickResult.builder().change(9, ItemStack.AIR).change(PlayerInventory.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE)).build()
        );

        assertPlayerClick(
                ClickResult.builder().change(PlayerInventory.CHESTPLATE_SLOT, ItemStack.of(Material.IRON_CHESTPLATE)).build(),
                new ClickInfo.ShiftClick(PlayerInventory.CHESTPLATE_SLOT),
                ClickResult.builder().change(PlayerInventory.CHESTPLATE_SLOT, ItemStack.AIR).change(9, ItemStack.of(Material.IRON_CHESTPLATE)).build()
        );

        assertPlayerClick(
                ClickResult.builder().change(9, ItemStack.of(Material.SHIELD)).build(),
                new ClickInfo.ShiftClick(9),
                ClickResult.builder().change(9, ItemStack.AIR).change(PlayerInventory.OFFHAND_SLOT, ItemStack.of(Material.SHIELD)).build()
        );

        assertPlayerClick(
                ClickResult.builder().change(PlayerInventory.OFFHAND_SLOT, ItemStack.of(Material.SHIELD)).build(),
                new ClickInfo.ShiftClick(PlayerInventory.OFFHAND_SLOT),
                ClickResult.builder().change(PlayerInventory.OFFHAND_SLOT, ItemStack.AIR).change(9, ItemStack.of(Material.SHIELD)).build()
        );
    }

}
