package net.minestom.server.item;

public enum ItemFlag {
    HIDE_ENCHANTS(1),
    HIDE_ATTRIBUTES(2),
    HIDE_UNBREAKABLE(4),
    HIDE_DESTROYS(8),
    HIDE_PLACED_ON(16),
    HIDE_POTION_EFFECTS(32);

    private final int bitFieldPart;
    ItemFlag(int bit) {
        this.bitFieldPart = bit;
    }

    public int getBitFieldPart() {
        return bitFieldPart;
    }
}
