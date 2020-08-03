package net.minestom.server.item;

/**
 * Represent the stacking rule of an item
 * This can be used to mimic the vanilla one (using the item amount displayed)
 * or a complete new one which can be stored in lore, name, etc...
 */
public abstract class StackingRule {

    private final int maxSize;

    public StackingRule(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Used to know if two ItemStack can be stacked together
     *
     * @param item1 the first item
     * @param item2 the second item
     * @return true if both item can be stacked together (do not take their amount in consideration)
     */
    public abstract boolean canBeStacked(ItemStack item1, ItemStack item2);

    /**
     * Used to know if an ItemStack can have the size {@code newAmount} applied
     *
     * @param item      the item to check
     * @param newAmount the desired new amount
     * @return true if item can have its stack size set to newAmount
     */
    public abstract boolean canApply(ItemStack item, int newAmount);

    /**
     * Change the size of the item to {@code newAmount}
     * At this point we know that the item can have this stack size applied
     *
     * @param item      the item stack to applies the size to
     * @param newAmount the new item size
     * @return the new ItemStack with the new amount
     */
    public abstract ItemStack apply(ItemStack item, int newAmount);

    /**
     * Used to determine the current stack size of an item
     * It is possible to have it stored in its Data object, lore, etc...
     *
     * @param itemStack the item stack to check the size
     * @return the correct size of itemStack
     */
    public abstract int getAmount(ItemStack itemStack);

    /**
     * Get the max size of a stack
     *
     * @return the max size of a stack
     */
    public int getMaxSize() {
        return maxSize;
    }
}
