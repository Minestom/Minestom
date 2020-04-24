package net.minestom.server.item;

public abstract class StackingRule {

    private int maxSize;

    public StackingRule(int maxSize) {
        this.maxSize = maxSize;
    }

    public abstract boolean canBeStacked(ItemStack item1, ItemStack item2);

    public abstract boolean canApply(ItemStack item, int newAmount);

    public abstract ItemStack apply(ItemStack item, int newAmount);

    public abstract int getAmount(ItemStack itemStack);

    public int getMaxSize() {
        return maxSize;
    }
}
