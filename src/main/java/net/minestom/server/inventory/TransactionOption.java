package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface TransactionOption<T> {

    /**
     * Performs as much of the operation as is possible.
     * Returns the remaining item in the operation (can be air).
     */
    TransactionOption<ItemStack> ALL = (inventory, result, itemChangesMap) -> {
        itemChangesMap.forEach(inventory::setItemStack);
        return result;
    };

    /**
     * Performs the operation atomically: i.e., only if the operation resulted in air, returning whether or not the
     * operation was performed.
     */
    TransactionOption<Boolean> ALL_OR_NOTHING = (inventory, result, itemChangesMap) -> {
        if (result.isAir()) {
            // Item can be fully placed inside the inventory, do so
            itemChangesMap.forEach(inventory::setItemStack);
            return true;
        } else {
            // Inventory cannot accept the item fully
            return false;
        }
    };

    /**
     * Discards the result of the operation, returning whether or not the operation could have finished.
     */
    TransactionOption<Boolean> DRY_RUN = (inventory, result, itemChangesMap) -> result.isAir();

    @NotNull T fill(@NotNull Inventory inventory, @NotNull ItemStack result, @NotNull Int2ObjectMap<ItemStack> itemChangesMap);

    default @NotNull T fill(@NotNull TransactionType type, @NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        Int2ObjectMap<ItemStack> changes = new Int2ObjectArrayMap<>();

        Int2ObjectFunction<ItemStack> function = new Int2ObjectFunction<>() {
            @Override
            public ItemStack get(int key) {
                return changes.containsKey(key) ? changes.get(key) : inventory.getItemStack(key);
            }

            @Override
            public ItemStack put(int key, ItemStack value) {
                var get = get(key);
                changes.put(key, value);
                return get;
            }
        };

        ItemStack result = type.process(itemStack, function);

        return fill(inventory, result, changes);
    }
}
