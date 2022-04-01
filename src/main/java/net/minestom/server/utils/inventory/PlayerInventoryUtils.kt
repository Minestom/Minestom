package net.minestom.server.utils.inventory

import net.minestom.server.utils.inventory.PlayerInventoryUtils

object PlayerInventoryUtils {
    const val OFFSET = 9
    const val CRAFT_RESULT = 36
    const val CRAFT_SLOT_1 = 37
    const val CRAFT_SLOT_2 = 38
    const val CRAFT_SLOT_3 = 39
    const val CRAFT_SLOT_4 = 40
    const val HELMET_SLOT = 41
    const val CHESTPLATE_SLOT = 42
    const val LEGGINGS_SLOT = 43
    const val BOOTS_SLOT = 44
    const val OFFHAND_SLOT = 45

    /**
     * Converts a packet slot to an internal one.
     *
     * @param slot   the packet slot
     * @param offset the slot count separating the up part of the inventory to the bottom part (armor/craft in PlayerInventory, inventory slots in others)
     * the offset for the player inventory is [.OFFSET]
     * @return a packet which can be use internally with Minestom
     */
    fun convertPlayerInventorySlot(slot: Int, offset: Int): Int {
        return when (slot) {
            0 -> CRAFT_RESULT
            1 -> CRAFT_SLOT_1
            2 -> CRAFT_SLOT_2
            3 -> CRAFT_SLOT_3
            4 -> CRAFT_SLOT_4
            5 -> HELMET_SLOT
            6 -> CHESTPLATE_SLOT
            7 -> LEGGINGS_SLOT
            8 -> BOOTS_SLOT
            else -> convertSlot(slot, offset)
        }
    }

    fun convertSlot(slot: Int, offset: Int): Int {
        var slot = slot
        val rowSize = 9
        slot -= offset
        slot = if (slot >= rowSize * 3 && slot < rowSize * 4) {
            slot % 9
        } else {
            slot + rowSize
        }
        return slot
    }

    /**
     * Used to convert internal slot to one used in packets
     *
     * @param slot the internal slot
     * @return a slot id which can be used for packets
     */
    fun convertToPacketSlot(slot: Int): Int {
        var slot = slot
        if (slot > -1 && slot < 9) { // Held bar 0-8
            slot = slot + 36
        } else if (slot > 8 && slot < 36) { // Inventory 9-35
            slot = slot
        } else if (slot >= CRAFT_RESULT && slot <= CRAFT_SLOT_4) { // Crafting 36-40
            slot = slot - 36
        } else if (slot >= HELMET_SLOT && slot <= BOOTS_SLOT) { // Armor 41-44
            slot = slot - 36
        } else if (slot == OFFHAND_SLOT) { // Off hand
            slot = 45
        }
        return slot
    }
}