package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

/**
 * Represents a type of transaction that you can apply to an {@link Inventory}.
 */
public interface TransactionType extends BiFunction<@NotNull ItemStack, @NotNull IntFunction<ItemStack>, TransactionType.@NotNull Entry> {

    /**
     * Applies a transaction operator to a given list of slots, turning it into a TransactionType.
     */
    static @NotNull TransactionType general(@NotNull TransactionOperator operator, @NotNull List<Integer> slots) {
        return (item, getter) -> {
            Int2ObjectMap<ItemStack> map = new Int2ObjectArrayMap<>();
            for (int slot : slots) {
                final ItemStack slotItem = getter.apply(slot);
                final TransactionOperator.Entry result = operator.apply(slotItem, item);
                if (result == null) continue;

                map.put(slot, result.left());
                item = result.right();
            }
            return new Entry(item, map);
        };
    }

    /**
     * Joins two transaction types consecutively.
     * This will use the same getter in both cases, so ensure that any potential overlap between the transaction types
     * will not result in unexpected behaviour (e.g. item duping).
     */
    static @NotNull TransactionType join(@NotNull TransactionType first, @NotNull TransactionType second) {
        return (item, getter) -> {
            // Calculate results
            final Entry f = first.apply(item, getter);
            final Entry s = second.apply(f.remaining(), getter);
            // Join results
            Map<Integer, ItemStack> map = new Int2ObjectArrayMap<>();
            map.putAll(f.changes());
            map.putAll(s.changes());
            return new Entry(s.remaining(), map);
        };
    }

    /**
     * Adds an item to the inventory.
     * Can either take an air slot or be stacked.
     *
     * @param fill the list of slots that will be added to if they already have some of the item in it
     * @param air  the list of slots that will be added to if they have air (may be different from {@code fill}).
     */
    static @NotNull TransactionType add(@NotNull List<Integer> fill, @NotNull List<Integer> air) {
        final TransactionType first = general(entry -> {
            final ItemStack slotItem = entry.left();
            final ItemStack extra = entry.right();
            return !slotItem.isAir() ? TransactionOperator.STACK_LEFT.apply(slotItem, extra) : null;
        }, fill);
        final TransactionType second = general(entry -> {
            final ItemStack slotItem = entry.left();
            final ItemStack extra = entry.right();
            return slotItem.isAir() ? TransactionOperator.STACK_LEFT.apply(slotItem, extra) : null;
        }, air);
        return TransactionType.join(first, second);
    }

    /**
     * Takes an item from the inventory.
     * Can either transform items to air or reduce their amount.
     *
     * @param takeSlots the ordered list of slots that will be taken from (if possible)
     */
    static @NotNull TransactionType take(@NotNull List<Integer> takeSlots) {
        return general(TransactionOperator.TAKE, takeSlots);
    }

    /**
     * Processes the provided item into the given inventory.
     *
     * @param itemStack the item to process
     * @param inventory the inventory function
     * @return the remaining portion of the processed item, as well as the changes
     */
    @Override
    @NotNull
    Entry apply(@NotNull ItemStack itemStack, @NotNull IntFunction<ItemStack> inventory);

    record Entry(@NotNull ItemStack remaining, @NotNull Map<@NotNull Integer, @NotNull ItemStack> changes) {
        public Entry {
            changes = Map.copyOf(changes);
        }
    }
}
