package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Represents a type of transaction that you can apply to an {@link AbstractInventory}.
 */
public interface TransactionType {

    /**
     * Adds an item to the inventory.
     * Can either take an air slot or be stacked.
     */
    static @NotNull TransactionType add(@NotNull Supplier<IntIterator> fillSlots, @NotNull Supplier<IntIterator> airSlots) {
        return (inventory, itemStack) -> {
            final var RULE = StackingRule.get();

            var slots = fillSlots.get();
            Int2ObjectMap<ItemStack> changes = new Int2ObjectArrayMap<>();

            var itemAmount = RULE.getAmount(itemStack);

            while (slots.hasNext() && itemAmount > 0) {
                var next = slots.nextInt();
                var slotItem = inventory.getItemStack(next);

                if (slotItem.isAir() || !RULE.canBeStacked(itemStack, slotItem)) continue;

                var maxSize = RULE.getMaxSize(slotItem);
                if (itemAmount >= maxSize) continue;

                var sum = itemAmount + RULE.getAmount(slotItem);

                if (sum <= maxSize) {
                    changes.put(next, RULE.apply(slotItem, sum));
                    itemAmount = 0;
                    break;
                } else {
                    changes.put(next, RULE.apply(slotItem, maxSize));
                    itemAmount = sum - maxSize;
                }
            }

            if (itemAmount > 0) { // Deposit the remaining amount in the first air slot.
                var air = airSlots.get();
                while (air.hasNext()) {
                    var next = air.nextInt();
                    var slotItem = inventory.getItemStack(next);

                    if (!slotItem.isAir()) continue;

                    changes.put(next, RULE.apply(itemStack, itemAmount));
                    itemAmount = 0;

                    break;
                }
            }

            return Pair.of(RULE.apply(itemStack, itemAmount), changes);
        };
    }

    /**
     * Takes an item from the inventory.
     * Can either transform items to air or reduce their amount.
     */
    static @NotNull TransactionType take(@NotNull Supplier<IntIterator> takeSlots) {
        return (inventory, itemStack) -> {
            final var RULE = StackingRule.get();
            var slots = takeSlots.get();

            Int2ObjectMap<ItemStack> changes = new Int2ObjectArrayMap<>();

            var remainingAmount = RULE.getAmount(itemStack);

            while (slots.hasNext() && remainingAmount > 0) {
                var next = slots.nextInt();
                var slotItem = inventory.getItemStack(next);

                if (slotItem.isAir() || !RULE.canBeStacked(itemStack, slotItem)) continue;

                var slotAmount = RULE.getAmount(slotItem);

                if (slotAmount < remainingAmount) {
                    remainingAmount -= slotAmount;
                    changes.put(next, ItemStack.AIR);
                } else {
                    remainingAmount = 0;
                    changes.put(next, RULE.apply(slotItem, count -> count - slotAmount));
                }
            }

            return Pair.of(RULE.apply(itemStack, remainingAmount), changes);
        };
    }

    @NotNull Pair<ItemStack, Int2ObjectMap<ItemStack>> process(@NotNull AbstractInventory inventory, @NotNull ItemStack itemStack);

}
