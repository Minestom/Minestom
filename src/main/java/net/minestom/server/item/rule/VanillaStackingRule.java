package net.minestom.server.item.rule;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

public final class VanillaStackingRule implements StackingRule {

    @Override
    public boolean canBeStacked(@NotNull ItemStack item1, @NotNull ItemStack item2) {
        return item1.isSimilar(item2);
    }

    @Override
    public boolean canApply(@NotNull ItemStack item, int newAmount) {
        return MathUtils.isBetween(newAmount, 0, getMaxSize(item));
    }

    @Override
    public @NotNull ItemStack apply(@NotNull ItemStack item, int amount) {
        return amount > 0 ? item.withAmount(amount) : ItemStack.AIR;
    }

    @Override
    public int getAmount(@NotNull ItemStack itemStack) {
        return itemStack.getAmount();
    }

    @Override
    public int getMaxSize(@NotNull ItemStack itemStack) {
        return itemStack.getMaterial().maxStackSize();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass();
    }
}
