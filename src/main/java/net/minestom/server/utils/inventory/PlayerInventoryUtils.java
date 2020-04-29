package net.minestom.server.utils.inventory;

public class PlayerInventoryUtils {

    public static final int OFFSET = 9;

    public static final int CRAFT_RESULT = 36;
    public static final int CRAFT_SLOT_1 = 37;
    public static final int CRAFT_SLOT_2 = 38;
    public static final int CRAFT_SLOT_3 = 39;
    public static final int CRAFT_SLOT_4 = 40;

    public static final int HELMET_SLOT = 41;
    public static final int CHESTPLATE_SLOT = 42;
    public static final int LEGGINGS_SLOT = 43;
    public static final int BOOTS_SLOT = 44;
    public static final int OFFHAND_SLOT = 45;

    public static int convertSlot(int slot, int offset) {
        switch (slot) {
            case 0:
                return CRAFT_RESULT;
            case 1:
                return CRAFT_SLOT_1;
            case 2:
                return CRAFT_SLOT_2;
            case 3:
                return CRAFT_SLOT_3;
            case 4:
                return CRAFT_SLOT_4;
            case 5:
                return HELMET_SLOT;
            case 6:
                return CHESTPLATE_SLOT;
            case 7:
                return LEGGINGS_SLOT;
            case 8:
                return BOOTS_SLOT;
        }
        //System.out.println("ENTRY: " + slot + " | " + offset);
        final int rowSize = 9;
        slot -= offset;
        if (slot >= rowSize * 3 && slot < rowSize * 4) {
            slot = slot % 9;
        } else {
            slot = slot + rowSize;
        }
        //System.out.println("CONVERT: " + slot);
        return slot;
    }
}
