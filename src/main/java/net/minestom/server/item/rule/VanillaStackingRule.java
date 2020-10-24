package net.minestom.server.item.rule;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;

public class VanillaStackingRule extends StackingRule {

    public VanillaStackingRule(int maxSize) {
        super(maxSize);
    }

    @Override
    public boolean canBeStacked(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        return item1.isSimilar(item2);
    }

    @Override
    public boolean canApply(@NotNull ItemStack item, int newAmount) {
        return newAmount > 0 && newAmount <= getMaxSize();
    }

    @NotNull
    @Override
    public ItemStack apply(@NotNull ItemStack item, int newAmount) {
        if (newAmount <= 0)
            return ItemStack.getAirItem();

        item.setAmount((byte) newAmount);
        return item;
    }

    @Override
    public int getAmount(@NotNull ItemStack itemStack) {
        return itemStack.getAmount();
    }
}
