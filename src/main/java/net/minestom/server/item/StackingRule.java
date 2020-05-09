package net.minestom.server.item;

public abstract class StackingRule {

    private int maxSize;

    public StackingRule(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @param item1 the first item
     * @param item2 the second item
     * @return true if both item can be stacked together (do not take their amount in consideration)
     */
    public abstract boolean canBeStacked(ItemStack item1, ItemStack item2);

    /**
     * @param item      the item to check
     * @param newAmount the desired new amount
     * @return true if item can have its stack size set to newAmount
     */
    public abstract boolean canApply(ItemStack item, int newAmount);

    /**
     * At this point we know that the item can have this stack size
     *
     * @param item
     * @param newAmount
     * @return the new ItemStack with the new amount
     */
    public abstract ItemStack apply(ItemStack item, int newAmount);

    /**
     * Used to determine the current stack size of an item
     * It is possible to have it stored in its Data object, lore, etc...
     *
     * @param itemStack
     * @return the correct size of itemStack
     */
    public abstract int getAmount(ItemStack itemStack);

    public int getMaxSize() {
        return maxSize;
    }
}
