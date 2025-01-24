package net.minestom.server.utils.inventory;

public final class PlayerInventoryUtils {
    /*
    There are 3 different slot mappings discussed in this file:
    - Minestom slots
    - Player inventory slots
    - Window slots

    *Minestom slots* represent all inventory slots, including items and player inventory specifics
      like the crafting grid, armor, and off hand. Those ids are specific to PlayerInventory and
      are mapped as follows:
      0-8: Hotbar
      9-35: Inventory
      36-40: Crafting grid
      41-44: Armor
      45: Offhand

    *Player inventory slots* represent the vanilla inventory slots, specifically the hotbar, 3 row
      inventory, armor slots, and off hand (NOT player crafting grid slots). Those ids are as follows:
      0-8: Hotbar
      9-35: Inventory
      36-39: Boots, Leggings, Chestplate, Helmet
      40: Offhand

    *Window slots* represent the slots in a window. Window id=0 represents the player crafting grid
      inventory. These slots start with W slots (where W = openInventory.getSize()) followed by the
      3 row player inventory and then the hotbar.
      0-W: Open inventory content
      W-(W+27): Player inventory content
      (W+27)-(W+36): Hotbar

      Window id=0 has special content in the first 9 slots:
      0: 2x2 crafting result
      1-4: 2x2 crafting grid
      5-8: Armor slots
     */

    public static final int WINDOW_0_OFFSET = 9;

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
     * Returns true if the given minestom slot is on the hotbar or offhand, false otherwise.
     */
    public static boolean isHotbarOrOffHandSlot(int minestomSlot) {
        return (minestomSlot >= 0 && minestomSlot < 9) || minestomSlot == OFFHAND_SLOT;
    }

    /**
     * Converts a window packet slot to a Minestom one.
     *
     * @param slot   the packet slot
     * @return a slot which can be use internally with Minestom
     */
    public static int convertWindow0SlotToMinestomSlot(int slot) {
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
            default -> convertWindowSlotToMinestomSlot(slot, WINDOW_0_OFFSET);
        };
    }

    public static int convertWindowSlotToMinestomSlot(int slot, int offset) {
        final int rowSize = 9;
        slot -= offset;
        if (slot >= rowSize * 3 && slot < rowSize * 4) {
            slot = slot % 9; // Hotbar
        } else {
            slot = slot + rowSize; // Rest of inventory
        }
        return slot;
    }

    /**
     * Returns true if the given Minestom slot is valid as a Player inventory slot (ie is it not a crafting grid slot)
     */
    public static boolean isPlayerInventorySlot(int minestomSlot) {
        return !(minestomSlot >= CRAFT_RESULT && minestomSlot <= CRAFT_SLOT_4);
    }

    /**
     * Used to convert a Minestom slot to a player inventory slot. Only valid for some slots, should be tested
     * with {@link #isPlayerInventorySlot(int)} first.
     */
    public static int convertMinestomSlotToPlayerInventorySlot(int minestomSlot) {
        if (minestomSlot >= HELMET_SLOT && minestomSlot <= BOOTS_SLOT) {
            // Armor is in the reverse order Minestom tracks it, and immediately after the main inventory
            return (3 - (minestomSlot - HELMET_SLOT)) + 36;
        } else if (minestomSlot == OFFHAND_SLOT) {
            return 40;
        }
        return minestomSlot;
    }

    /**
     * Used to convert internal slot to one used in packets
     *
     * @param slot the internal slot
     * @return a slot id which can be used for packets
     */
    public static int convertMinestomSlotToWindowSlot(int slot) {
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
     * Used to convert a player inventory slot from a client to a Minestom slot.
     * See above for description
     *
     * @param slot the client slot
     * @return a slot which can be used internally with Minestom
     */
    public static int convertPlayerInventorySlotToMinestomSlot(int slot) {
        if (slot < 0 || slot > 40) return -1; // Sanity
        // Armor slots are reversed in Minestom, and off hand is a different slot
        if (slot == 36) return BOOTS_SLOT;
        if (slot == 37) return LEGGINGS_SLOT;
        if (slot == 38) return CHESTPLATE_SLOT;
        if (slot == 39) return HELMET_SLOT;
        if (slot == 40) return OFFHAND_SLOT;
        return slot;
    }
}
