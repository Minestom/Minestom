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
            assertEquals(i, convertWindow0SlotToMinestomSlot(i + 36));
        }
    }

    @Test
    public void mainInventory() {
        // No conversion, slots should stay 9-35
        for (int i = 9; i < 9 * 4; i++) {
            assertEquals(i, convertWindow0SlotToMinestomSlot(i));
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
        assertEquals(HELMET_SLOT, convertWindow0SlotToMinestomSlot(5));
        assertEquals(CHESTPLATE_SLOT, convertWindow0SlotToMinestomSlot(6));
        assertEquals(LEGGINGS_SLOT, convertWindow0SlotToMinestomSlot(7));
        assertEquals(BOOTS_SLOT, convertWindow0SlotToMinestomSlot(8));
        assertEquals(OFFHAND_SLOT, convertWindow0SlotToMinestomSlot(45));
    }

    @Test
    public void craft() {
        assertEquals(CRAFT_RESULT, 36);
        assertEquals(CRAFT_SLOT_1, 37);
        assertEquals(CRAFT_SLOT_2, 38);
        assertEquals(CRAFT_SLOT_3, 39);
        assertEquals(CRAFT_SLOT_4, 40);

        // Convert 0-4 into 36-40
        assertEquals(CRAFT_RESULT, convertWindow0SlotToMinestomSlot(0));
        assertEquals(CRAFT_SLOT_1, convertWindow0SlotToMinestomSlot(1));
        assertEquals(CRAFT_SLOT_2, convertWindow0SlotToMinestomSlot(2));
        assertEquals(CRAFT_SLOT_3, convertWindow0SlotToMinestomSlot(3));
        assertEquals(CRAFT_SLOT_4, convertWindow0SlotToMinestomSlot(4));
    }
}
