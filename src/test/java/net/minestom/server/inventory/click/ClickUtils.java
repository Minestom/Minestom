package net.minestom.server.inventory.click;

import net.minestom.server.api.TestUtils;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClickUtils {
    void assertSingleClick(Function<Inventory, ClickResult.Single> filler,
                           ItemStack expectedRemaining,
                           Map<Integer, ItemStack> expectedChangedSlots) {
        Inventory inventory = new Inventory(InventoryType.HOPPER, "test");
        var result = filler.apply(inventory);
        assertEquals(expectedRemaining, result.remaining(), "Invalid remaining");
        assertEquals(expectedChangedSlots, result.changedSlots(), "Invalid changed slots");
    }

    void assertPlayerSingleClick(Function<PlayerInventory, ClickResult.Single> filler,
                                 ItemStack expectedRemaining,
                                 Map<Integer, ItemStack> expectedChangedSlots) {
        var player = TestUtils.createDummyPlayer();
        PlayerInventory inventory = new PlayerInventory(player);
        var result = filler.apply(inventory);
        assertEquals(expectedRemaining, result.remaining(), "Invalid remaining");
        assertEquals(expectedChangedSlots, result.changedSlots(), "Invalid changed slots");
    }

    void assertDouble(BiFunction<PlayerInventory, Inventory, ClickResult.Double> filler,
                      ItemStack expectedRemaining,
                      Map<Integer, ItemStack> playerChanges,
                      Map<Integer, ItemStack> inventoryChanges) {
        var player = TestUtils.createDummyPlayer();
        PlayerInventory playerInventory = new PlayerInventory(player);
        Inventory inventory = new Inventory(InventoryType.HOPPER, "test");
        var result = filler.apply(playerInventory, inventory);
        assertEquals(expectedRemaining, result.remaining(), "Invalid remaining");
        assertEquals(playerChanges, result.playerChanges(), "Invalid player changes");
        assertEquals(inventoryChanges, result.inventoryChanges(), "Invalid inventory changes");
    }
}
