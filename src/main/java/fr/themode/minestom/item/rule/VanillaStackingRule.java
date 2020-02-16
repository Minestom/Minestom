package fr.themode.minestom.item.rule;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.StackingRule;

public class VanillaStackingRule extends StackingRule {

    public VanillaStackingRule(int maxSize) {
        super(maxSize);
    }

    @Override
    public boolean canApply(ItemStack item, int newAmount) {
        return newAmount >= 1 && newAmount <= getMaxSize();
    }

    @Override
    public ItemStack apply(ItemStack item, int newAmount) {
        if (newAmount <= 0)
            return ItemStack.AIR_ITEM;

        item.setAmount((byte) newAmount);
        return item;
    }

    @Override
    public int getAmount(ItemStack itemStack) {
        return itemStack.getAmount();
    }
}
