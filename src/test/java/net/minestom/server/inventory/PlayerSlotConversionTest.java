package net.minestom.server.inventory;

import org.junit.jupiter.api.Test;

import static net.minestom.server.utils.inventory.PlayerInventoryUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test conversion from packet slots to internal ones (used in events and inventory methods)
 */
public class PlayerSlotConversionTest {

    @Test
    public void hotbar() {
        // Convert 36-44 into 0-8
        for (int i = 0; i < 9; i++) {
            assertEquals(i, convertPlayerInventorySlot(i + 36, OFFSET));
        }
    }

    @Test
    public void mainInventory() {
        // No conversion, slots should stay 9-35
        for (int i = 9; i < 9 * 4; i++) {
            assertEquals(i, convertPlayerInventorySlot(i, OFFSET));
        }
    }

    @Test
    public void armor() {
        assertEquals(HELMET_SLOT, 41);
        assertEquals(CHESTPLATE_SLOT, 42);
        assertEquals(LEGGINGS_SLOT, 43);
        assertEquals(BOOTS_SLOT, 44);
        assertEquals(OFFHAND_SLOT, 45);

        // Convert 5-8 & 45 into 41-45
        assertEquals(HELMET_SLOT, convertPlayerInventorySlot(5, OFFSET));
        assertEquals(CHESTPLATE_SLOT, convertPlayerInventorySlot(6, OFFSET));
        assertEquals(LEGGINGS_SLOT, convertPlayerInventorySlot(7, OFFSET));
        assertEquals(BOOTS_SLOT, convertPlayerInventorySlot(8, OFFSET));
        assertEquals(OFFHAND_SLOT, convertPlayerInventorySlot(45, OFFSET));
    }

    @Test
    public void craft() {
        assertEquals(CRAFT_RESULT, 36);
        assertEquals(CRAFT_SLOT_1, 37);
        assertEquals(CRAFT_SLOT_2, 38);
        assertEquals(CRAFT_SLOT_3, 39);
        assertEquals(CRAFT_SLOT_4, 40);

        // Convert 0-4 into 36-40
        assertEquals(CRAFT_RESULT, convertPlayerInventorySlot(0, OFFSET));
        assertEquals(CRAFT_SLOT_1, convertPlayerInventorySlot(1, OFFSET));
        assertEquals(CRAFT_SLOT_2, convertPlayerInventorySlot(2, OFFSET));
        assertEquals(CRAFT_SLOT_3, convertPlayerInventorySlot(3, OFFSET));
        assertEquals(CRAFT_SLOT_4, convertPlayerInventorySlot(4, OFFSET));
    }
}
