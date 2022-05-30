package net.minestom.server.utils.inventory;

public final class PlayerInventoryUtils {

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

    private PlayerInventoryUtils() {

    }

    /**
     * Converts a packet slot to an internal one.
     *
     * @param slot   the packet slot
     * @param offset the slot count separating the up part of the inventory to the bottom part (armor/craft in PlayerInventory, inventory slots in others)
     *               the offset for the player inventory is {@link #OFFSET}
     * @return a packet which can be use internally with Minestom
     */
    public static int convertPlayerInventorySlot(int slot, int offset) {
        return switch (slot) {
            case 0 -> CRAFT_RESULT;
            case 1 -> CRAFT_SLOT_1;
            case 2 -> CRAFT_SLOT_2;
            case 3 -> CRAFT_SLOT_3;
            case 4 -> CRAFT_SLOT_4;
            case 5 -> HELMET_SLOT;
            case 6 -> CHESTPLATE_SLOT;
            case 7 -> LEGGINGS_SLOT;
            case 8 -> BOOTS_SLOT;
            default -> convertSlot(slot, offset);
        };

    }

    public static int convertSlot(int slot, int offset) {
        final int rowSize = 9;
        slot -= offset;
        if (slot >= rowSize * 3 && slot < rowSize * 4) {
            slot = slot % 9;
        } else {
            slot = slot + rowSize;
        }
        return slot;
    }


    /**
     * Used to convert internal slot to one used in packets
     *
     * @param slot the internal slot
     * @return a slot id which can be used for packets
     */
    public static int convertToPacketSlot(int slot) {
        if (slot > -1 && slot < 9) { // Held bar 0-8
            slot = slot + 36;
        } else if (slot > 8 && slot < 36) { // Inventory 9-35
            slot = slot;
        } else if (slot >= CRAFT_RESULT && slot <= CRAFT_SLOT_4) { // Crafting 36-40
            slot = slot - 36;
        } else if (slot >= HELMET_SLOT && slot <= BOOTS_SLOT) { // Armor 41-44
            slot = slot - 36;
        } else if (slot == OFFHAND_SLOT) { // Off hand
            slot = 45;
        }
        return slot;
    }

    /**
     * Used to convert the clients inventory slot to a Minestom slot.
     * The client's inventory does not count the crafting slots.
     *
     * @param slot the client slot
     * @return a slot which can be used internally with Minestom
     */
    public static int convertClientInventorySlot(int slot) {
        if (slot == 36) return BOOTS_SLOT;
        if (slot == 37) return LEGGINGS_SLOT;
        if (slot == 38) return CHESTPLATE_SLOT;
        if (slot == 39) return HELMET_SLOT;
        if (slot == 40) return OFFHAND_SLOT;
        return slot;
    }
}
