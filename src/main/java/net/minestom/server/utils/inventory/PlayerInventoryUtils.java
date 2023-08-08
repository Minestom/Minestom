package net.minestom.server.utils.inventory;


/**
 * Minestom uses different slot IDs for player inventories as the Minecraft protocol uses a strange system (e.g. the
 * crafting result is the first slot).<br>
 * These can be mapped 1:1 to and from protocol slots using {@link #minestomToProtocol(int)} and {@link #protocolToMinestom(int)}.<br>
 *
 * Read about protocol slot IDs <a href="https://wiki.vg/Inventory">here</a>.
 */
public final class PlayerInventoryUtils {

    public static final int INVENTORY_SIZE = 46;
    public static final int INNER_SIZE = 36;

    public static final int PROTOCOL_OFFSET = 9;

    public static final int CRAFT_RESULT = 36;
    public static final int CRAFT_SLOT_1 = 37;
    public static final int CRAFT_SLOT_2 = 38;
    public static final int CRAFT_SLOT_3 = 39;
    public static final int CRAFT_SLOT_4 = 40;

    public static final int HELMET_SLOT = 41;
    public static final int CHESTPLATE_SLOT = 42;
    public static final int LEGGINGS_SLOT = 43;
    public static final int BOOTS_SLOT = 44;
    public static final int OFF_HAND_SLOT = 45;

    private PlayerInventoryUtils() {

    }

    /**
     * Converts a Minestom slot ID to a Minecraft protocol slot ID.<br>
     * This is the inverse of {@link #protocolToMinestom(int)}.
     * @param slot the internal slot ID to convert
     * @return the protocol slot ID, or -1 if the given slot could not be converted
     */
    public static int minestomToProtocol(int slot) {
        return switch (slot) {
            case CRAFT_RESULT -> 0;
            case CRAFT_SLOT_1 -> 1;
            case CRAFT_SLOT_2 -> 2;
            case CRAFT_SLOT_3 -> 3;
            case CRAFT_SLOT_4 -> 4;
            case HELMET_SLOT -> 5;
            case CHESTPLATE_SLOT -> 6;
            case LEGGINGS_SLOT -> 7;
            case BOOTS_SLOT -> 8;
            case OFF_HAND_SLOT -> OFF_HAND_SLOT;
            default -> {
                if (slot >= 0 && slot <= 8) {
                    yield slot + 36;
                } else if (slot >= 9 && slot <= 35) {
                    yield slot;
                } else {
                    yield -1; // Unknown slot ID
                }
            }
        };
    }

    /**
     * Converts a Minecraft protocol slot ID to a Minestom slot ID.<br>
     * This is the inverse of {@link #minestomToProtocol(int)}.
     * @param slot the protocol slot ID to convert
     * @return the Minestom slot ID, or -1 if the given slot could not be converted
     */
    public static int protocolToMinestom(int slot) {
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
            case OFF_HAND_SLOT -> OFF_HAND_SLOT;
            default -> {
                if (slot >= 36 && slot <= 44) {
                    yield slot - 36;
                } else if (slot >= 9 && slot <= 35) {
                    yield slot;
                } else {
                    yield -1; // Unknown slot ID
                }
            }
        };
    }

    /**
     * Converts the given slot into a protocol ID directly after the provided inventory.
     * This is intended for when a player's inner inventory is interacted with while a player has another inventory
     * open.<br>
     * This is the inverse of {@link #protocolToMinestom(int, int)}.
     *
     * @param slot the player slot that was interacted with
     * @param openInventorySize the size of the inventory opened by the player (not the player's inventory)
     * @return the protocol slot ID
     */
    public static int minestomToProtocol(int slot, int openInventorySize) {
        return PlayerInventoryUtils.minestomToProtocol(slot) + openInventorySize - PROTOCOL_OFFSET;
    }

    /**
     * Converts the given protocol ID that is directly after the provided inventory's slots into a player inventory slot
     * ID. This is intended for when a player's inner inventory is interacted with while a player has another inventory
     * open.<br>
     * This is the inverse of {@link #minestomToProtocol(int, int)}.
     *
     * @param slot the protocol slot ID, situated directly after the slot IDs for the open inventory
     * @param openInventorySize the size of the inventory opened by the player (not the player's inventory)
     * @return the player slot ID
     */
    public static int protocolToMinestom(int slot, int openInventorySize) {
        if (slot < openInventorySize) return -1;

        return PlayerInventoryUtils.protocolToMinestom(slot - openInventorySize + PROTOCOL_OFFSET);
    }

}