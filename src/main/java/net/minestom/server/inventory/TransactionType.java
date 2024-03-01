package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a type of transaction that you can apply to an {@link Inventory}.
 */
public interface TransactionType {

    /**
     * Applies a transaction operator to a given list of slots, turning it into a TransactionType.
     */
    static @NotNull TransactionType general(@NotNull TransactionOperator operator, @NotNull IntIterable slots) {
        return (item, function) -> {
            IntIterator iterator = slots.iterator();
            while (iterator.hasNext()) {
                int slot = iterator.nextInt();

                ItemStack slotItem = function.get(slot);

                Pair<ItemStack, ItemStack> result = operator.apply(slotItem, item);
                if (result == null) continue;

                function.put(slot, result.first());
                item = result.second();
            }

            return item;
        };
    }

    /**
     * Joins two transaction types consecutively.
     */
    static @NotNull TransactionType join(@NotNull TransactionType first, @NotNull TransactionType second) {
        return (item, function) -> second.process(first.process(item, function), function);
    }

    /**
     * Adds an item to the inventory.
     * Can either take an air slot or be stacked.
     *
     * @param fill the list of slots that will be added to if they already have some of the item in it
     * @param air the list of slots that will be added to if they have air (may be different from {@code fill}).
     */
    static @NotNull TransactionType add(@NotNull IntList fill, @NotNull IntList air) {
        var first = general((slotItem, extra) -> !slotItem.isAir() ? TransactionOperator.STACK_LEFT.apply(slotItem, extra) : null, fill);
        var second = general((slotItem, extra) -> slotItem.isAir() ? TransactionOperator.STACK_LEFT.apply(slotItem, extra) : null, air);
        return TransactionType.join(first, second);
    }

    /**
     * Takes an item from the inventory.
     * Can either transform items to air or reduce their amount.
     * @param takeSlots the ordered list of slots that will be taken from (if possible)
     */
    static @NotNull TransactionType take(@NotNull IntList takeSlots) {
        return general(TransactionOperator.TAKE, takeSlots);
    }

    /**
     * Processes the provided item into the given inventory.
     * @param itemStack the item to process
     * @param inventory the inventory function; must support #get and #put operations.
     * @return the remaining portion of the processed item
     */
    @NotNull ItemStack process(@NotNull ItemStack itemStack, @NotNull Int2ObjectFunction<ItemStack> inventory);

}
