package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
     * Performs the operation atomically (only if the operation resulted in air), returning whether or not the operation
     * was performed.
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

    @NotNull T fill(@NotNull Inventory inventory, @NotNull ItemStack result, @NotNull Map<Integer, ItemStack> itemChangesMap);

    default @NotNull T fill(@NotNull TransactionType type, @NotNull Inventory inventory, @NotNull ItemStack itemStack) {
        Pair<ItemStack, Map<Integer, ItemStack>> result = type.process(itemStack, inventory::getItemStack);
        return fill(inventory, result.left(), result.right());
    }
}
