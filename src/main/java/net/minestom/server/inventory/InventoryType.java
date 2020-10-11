package net.minestom.server.inventory;

/**
 * Represents a type of {@link Inventory}
 */
public enum InventoryType {

    CHEST_1_ROW(0, 9),
    CHEST_2_ROW(1, 18),
    CHEST_3_ROW(2, 27),
    CHEST_4_ROW(3, 36),
    CHEST_5_ROW(4, 45),
    CHEST_6_ROW(5, 54),
    WINDOW_3X3(6, 9),
    ANVIL(7, 3),
    BEACON(8, 1),
    BLAST_FURNACE(9, 3),
    BREWING_STAND(10, 5),
    CRAFTING(11, 10),
    ENCHANTMENT(12, 2),
    FURNACE(13, 3),
    GRINDSTONE(14, 3),
    HOPPER(15, 5),
    LECTERN(16, 0),
    LOOM(17, 4),
    MERCHANT(18, 3),
    SHULKER_BOX(19, 27),
    SMOKER(20, 3),
    CARTOGRAPHY(21, 3),
    STONE_CUTTER(22, 2);

    private final int windowType;
    private final int slot;

    InventoryType(int windowType, int slot) {
        this.windowType = windowType;
        this.slot = slot;
    }

    public int getWindowType() {
        return windowType;
    }

    public int getAdditionalSlot() {
        return slot;
    }

}
