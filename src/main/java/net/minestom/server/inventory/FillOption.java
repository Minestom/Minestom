package net.minestom.server.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@FunctionalInterface
public interface FillOption<T> {

    FillOption<ItemStack> ALL = (inventory, result, itemChangesMap) -> {
        itemChangesMap.forEach(inventory::safeItemInsert);
        return result;
    };

    FillOption<Boolean> ALL_OR_NOTHING = (inventory, result, itemChangesMap) -> {
        if (result.isAir()) {
            // Item can be fully placed inside the inventory, do so
            itemChangesMap.forEach(inventory::safeItemInsert);
            return true;
        } else {
            // Inventory cannot accept the item fully
            return false;
        }
    };

    FillOption<Boolean> DRY_RUN = (inventory, result, itemChangesMap) -> !result.isAir();

    T fill(@NotNull AbstractInventory inventory,
           @NotNull ItemStack result,
           @NotNull Map<@NotNull Integer, @NotNull ItemStack> itemChangesMap);
}
