package net.minestom.server.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface TransactionOption<T> {

    /**
     * Place as much as the item as possible.
     * <p>
     * The remaining, can be air.
     */
    TransactionOption<ItemStack> ALL = (inventory, result, itemChangesMap) -> {
        itemChangesMap.forEach(inventory::safeItemInsert);
        return result;
    };

    /**
     * Only place the item if can be fully added.
     * <p>
     * Returns true if the item has been added, false if nothing changed.
     */
    TransactionOption<Boolean> ALL_OR_NOTHING = (inventory, result, itemChangesMap) -> {
        if (result.isAir()) {
            // Item can be fully placed inside the inventory, do so
            itemChangesMap.forEach(inventory::safeItemInsert);
            return true;
        } else {
            // Inventory cannot accept the item fully
            return false;
        }
    };

    /**
     * Loop through the inventory items without changing anything.
     * <p>
     * Returns true if the item can be fully added, false otherwise.
     */
    TransactionOption<Boolean> DRY_RUN = (inventory, result, itemChangesMap) -> result.isAir();

    @NotNull T fill(@NotNull AbstractInventory inventory,
                    @NotNull ItemStack result,
                    @NotNull Map<@NotNull Integer, @NotNull ItemStack> itemChangesMap);

    default @NotNull T fill(@NotNull TransactionType type,
                            @NotNull AbstractInventory inventory,
                            @NotNull ItemStack itemStack) {
        var pair = type.process(inventory, itemStack);
        return fill(inventory, pair.left(), pair.right());
    }
}
