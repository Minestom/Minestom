package net.minestom.server.item;

/**
 * Represents a hide flag which can be applied to an {@link ItemStack} using {@link ItemMetaBuilder#hideFlag(int)}.
 */
public enum ItemHideFlag {
    HIDE_ENCHANTS,
    HIDE_ATTRIBUTES,
    HIDE_UNBREAKABLE,
    HIDE_DESTROYS,
    HIDE_PLACED_ON,
    HIDE_POTION_EFFECTS,
    HIDE_DYE;

    private final int bitFieldPart = 1 << this.ordinal();

    public int getBitFieldPart() {
        return bitFieldPart;
    }
}
