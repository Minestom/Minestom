package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

/**
 * A transaction operator is a simpler operation that takes two items and returns two items.
 * <br>
 * This allows a significant amount of logic reuse, since many operations are just the {@link #flip(TransactionOperator) flipped}
 * version of others.
 */
public interface TransactionOperator {

    /**
     * Creates a new operator that filters the given one using the provided predicate
     */
    static @NotNull TransactionOperator filter(@NotNull TransactionOperator operator, @NotNull BiPredicate<ItemStack, ItemStack> predicate) {
        return (left, right) -> predicate.test(left, right) ? operator.apply(left, right) : null;
    }

    /**
     * Creates a new operator that flips the left and right before providing it to the given operator.
     */
    static @NotNull TransactionOperator flip(@NotNull TransactionOperator operator) {
        return (left, right) -> {
            var pair = operator.apply(right, left);
            return pair != null ? Pair.of(pair.right(), pair.left()) : null;
        };
    }

    /**
     * Provides operators that try to stack up to the provided number of items to the left.
     */
    static @NotNull TransactionOperator stackLeftN(int count) {
        return (left, right) -> {
            final StackingRule rule = StackingRule.get();

            // Quick exit if the right is air (nothing can be transferred anyway)
            // If the left is air then we know it can be transferred, but it can also be transferred if they're stackable
            // and left isn't full, even if left isn't air.
            if (right.isAir() || (!left.isAir() && !(rule.canBeStacked(left, right) && rule.getAmount(left) < rule.getMaxSize(left)))) {
                return null;
            }

            int leftAmount = left.isAir() ? 0 : rule.getAmount(left);
            int rightAmount = rule.getAmount(right);

            int addedAmount = Math.min(Math.min(rightAmount, count), rule.getMaxSize(left) - leftAmount);

            if (addedAmount == 0) return null;

            return Pair.of(rule.apply(left.isAir() ? right : left, leftAmount + addedAmount), rule.apply(right, rightAmount - addedAmount));
        };
    }

    /**
     * Stacks as many items to the left as possible, including if the left is an air item.<br>
     * This will not swap the items if they are of different types.
     */
    TransactionOperator STACK_LEFT = (left, right) -> {
        final StackingRule rule = StackingRule.get();

        // Quick exit if the right is air (nothing can be transferred anyway)
        // If the left is air then we know it can be transferred, but it can also be transferred if they're stackable
        // and left isn't full, even if left isn't air.
        if (right.isAir() || (!left.isAir() && !(rule.canBeStacked(left, right) && rule.getAmount(left) < rule.getMaxSize(left)))) {
            return null;
        }

        int leftAmount = left.isAir() ? 0 : rule.getAmount(left);
        int rightAmount = rule.getAmount(right);

        int addedAmount = Math.min(rightAmount, rule.getMaxSize(left) - leftAmount);

        return Pair.of(rule.apply(left.isAir() ? right : left, leftAmount + addedAmount), rule.apply(right, rightAmount - addedAmount));
    };

    /**
     * Stacks as many items to the right as possible. This is the flipped version of {@link #STACK_LEFT}.
     */
    TransactionOperator STACK_RIGHT = flip(STACK_LEFT);

    /**
     * Takes as many items as possible from both stacks, if the given items are stackable.
     * This is a symmetric operation.
     */
    TransactionOperator TAKE = (left, right) -> {
        final StackingRule rule = StackingRule.get();

        if (right.isAir() || !rule.canBeStacked(left, right)) {
            return null;
        }

        int leftAmount = rule.getAmount(left);
        int rightAmount = rule.getAmount(right);

        int subtracted = Math.min(leftAmount, rightAmount);

        return Pair.of(rule.apply(left, leftAmount - subtracted), rule.apply(right, rightAmount - subtracted));
    };

    /**
     * Applies this operation to the two given items.
     * They are unnamed as to abstract the operations performed from inventories.
     * @param left the "left" item
     * @param right the "right" item
     * @return the resulting pair, or null to indicate no changes
     */
    @Nullable Pair<ItemStack, ItemStack> apply(@NotNull ItemStack left, @NotNull ItemStack right);

}
