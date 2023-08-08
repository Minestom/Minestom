package net.minestom.server.inventory;

/**
 * Represents a type of {@link ContainerInventory}
 */
public enum InventoryType {

    CHEST_1_ROW(9),
    CHEST_2_ROW(18),
    CHEST_3_ROW(27),
    CHEST_4_ROW(36),
    CHEST_5_ROW(45),
    CHEST_6_ROW(54),
    WINDOW_3X3(9),
    CRAFTER_3X3(9),
    ANVIL(3),
    BEACON(1),
    BLAST_FURNACE(3),
    BREWING_STAND(5),
    CRAFTING(10),
    ENCHANTMENT(2),
    FURNACE(3),
    GRINDSTONE(3),
    HOPPER(5),
    LECTERN(1),
    LOOM(4),
    MERCHANT(3),
    SHULKER_BOX(27),
    SMITHING(4),
    SMOKER(3),
    CARTOGRAPHY(3),
    STONE_CUTTER(2);

    private final int size;

    InventoryType(int size) {
        this.size = size;
    }

    public int getWindowType() {
        return ordinal();
    }

    public int getSize() {
        return size;
    }

    /**
     * @deprecated use {@link #getSize()}
     */
    @Deprecated
    public int getAdditionalSlot() {
        return size;
    }

}
