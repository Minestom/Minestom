package net.minestom.server.item.rule;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
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
        return MathUtils.isBetween(newAmount, 0, getMaxSize());
    }

    @NotNull
    @Override
    public ItemStack apply(@NotNull ItemStack item, int newAmount) {
        if (newAmount <= 0)
            return ItemStack.AIR;

        return item.withAmount(newAmount);
    }

    @Override
    public @NotNull ItemStack merge(@NotNull ItemStack first, @NotNull ItemStack second) {
        int newAmount = first.getAmount() + second.getAmount();
        return apply(first, newAmount).withStore(builder -> builder.merge(second.getStore().builder()));
    }

    @Override
    public @NotNull Pair<@NotNull ItemStack, @NotNull ItemStack> split(@NotNull ItemStack item, int firstAmount) {
        Check.argCondition(firstAmount < 0 || firstAmount > getAmount(item), "Could not split item with provided amount");
        int secondAmount = getAmount(item) - firstAmount;
        var builders = item.getStore().builder().split(firstAmount, secondAmount);
        return Pair.of(
                apply(item, firstAmount).withStore(builders.left().build()),
                apply(item, secondAmount).withStore(builders.right().build())
        );
    }

    @Override
    public int getAmount(@NotNull ItemStack itemStack) {
        return itemStack.getAmount();
    }
}
