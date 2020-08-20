package net.minestom.server.inventory;

/**
 * List of inventory property and their ID
 * <p>
 * See https://wiki.vg/Protocol#Window_Property for more information
 */
public enum InventoryProperty {

    FURNACE_FIRE_ICON((short) 0),
    FURNACE_MAXIMUM_FUEL_BURN_TIME((short) 1),
    FURNACE_PROGRESS_ARROW((short) 2),
    FURNACE_MAXIMUM_PROGRESS((short) 3),

    ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_TOP((short) 0),
    ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_MIDDLE((short) 1),
    ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_BOTTOM((short) 2),
    ENCHANTMENT_TABLE_SEED((short) 3),
    ENCHANTMENT_TABLE_ENCH_ID_TOP((short) 4),
    ENCHANTMENT_TABLE_ENCH_ID_MIDDLE((short) 5),
    ENCHANTMENT_TABLE_ENCH_ID_BOTTOM((short) 6),
    ENCHANTMENT_TABLE_ENCH_LEVEL_TOP((short) 7),
    ENCHANTMENT_TABLE_ENCH_LEVEL_MIDDLE((short) 8),
    ENCHANTMENT_TABLE_ENCH_LEVEL_BOTTOM((short) 9),

    BEACON_POWER_LEVEL((short) 0),
    BEACON_FIRST_POTION((short) 1),
    BEACON_SECOND_POTION((short) 2),

    ANVIL_REPAIR_COST((short) 0),

    BREWING_STAND_BREW_TIME((short) 0),
    BREWING_STAND_FUEL_TIME((short) 1);


    private final short property;

    InventoryProperty(short property) {
        this.property = property;
    }

    public short getProperty() {
        return property;
    }
}
