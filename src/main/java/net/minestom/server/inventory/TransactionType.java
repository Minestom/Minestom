package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a type of transaction that you can apply to an {@link AbstractInventory}.
 */
@FunctionalInterface
public interface TransactionType {

    /**
     * Adds an item to the inventory.
     * Can either take an air slot or be stacked.
     */
    TransactionType ADD = (inventory, itemStack, slotPredicate, start, end, step) -> {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();
        // Check filled slot (not air)
        for (int i = start; step > 0 ? i < end : i > end; i += step) {
            ItemStack inventoryItem = inventory.getItemStack(i);
            if (inventoryItem.isAir()) {
                continue;
            }
            if (itemStack.isSimilar(inventoryItem)) {
                final int itemAmount = inventoryItem.amount();
                final int maxSize = inventoryItem.maxStackSize();
                if (itemAmount >= maxSize) continue;
                if (!slotPredicate.test(i, inventoryItem)) {
                    // Cancelled transaction
                    continue;
                }

                final int itemStackAmount = itemStack.amount();
                final int totalAmount = itemStackAmount + itemAmount;
                if (!MathUtils.isBetween(totalAmount, 0, itemStack.maxStackSize())) {
                    // Slot cannot accept the whole item, reduce amount to 'itemStack'
                    itemChangesMap.put(i, inventoryItem.withAmount(maxSize));
                    itemStack = itemStack.withAmount(totalAmount - maxSize);
                } else {
                    // Slot can accept the whole item
                    itemChangesMap.put(i, inventoryItem.withAmount(totalAmount));
                    itemStack = ItemStack.AIR;
                    break;
                }
            }
        }
        // Check air slot to fill
        for (int i = start; step > 0 ? i < end : i > end; i += step) {
            ItemStack inventoryItem = inventory.getItemStack(i);
            if (!inventoryItem.isAir()) continue;
            if (!slotPredicate.test(i, inventoryItem)) {
                // Cancelled transaction
                continue;
            }

            final int maxSize = itemStack.maxStackSize();
            final int currentSize = itemStack.amount();

            if (!MathUtils.isBetween(currentSize, 0, maxSize)) {
                // Slot cannot accept the whole item, reduce amount to 'itemStack'
                itemChangesMap.put(i, itemStack.withAmount(maxSize));
                itemStack = itemStack.withAmount(currentSize - maxSize);
            } else {
                // Slot can accept the whole item
                itemChangesMap.put(i, itemStack.withAmount(currentSize));
                itemStack = ItemStack.AIR;
                break;
            }
        }
        return Pair.of(itemStack, itemChangesMap);
    };

    /**
     * Takes an item from the inventory.
     * Can either transform items to air or reduce their amount.
     */
    TransactionType TAKE = (inventory, itemStack, slotPredicate, start, end, step) -> {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();
        for (int i = start; step > 0 ? i < end : i > end; i += step) {
            final ItemStack inventoryItem = inventory.getItemStack(i);
            if (inventoryItem.isAir()) continue;
            if (itemStack.isSimilar(inventoryItem)) {
                if (!slotPredicate.test(i, inventoryItem)) {
                    // Cancelled transaction
                    continue;
                }

                final int itemAmount = inventoryItem.amount();
                final int itemStackAmount = itemStack.amount();
                if (itemStackAmount < itemAmount) {
                    itemChangesMap.put(i, inventoryItem.withAmount(itemAmount - itemStackAmount));
                    itemStack = ItemStack.AIR;
                    break;
                }
                itemChangesMap.put(i, ItemStack.AIR);
                itemStack = itemStack.withAmount(itemStackAmount - itemAmount);
                if (itemStack.amount() == 0) {
                    itemStack = ItemStack.AIR;
                    break;
                }
            }
        }
        return Pair.of(itemStack, itemChangesMap);
    };

    @NotNull Pair<ItemStack, Map<Integer, ItemStack>> process(@NotNull AbstractInventory inventory,
                                                              @NotNull ItemStack itemStack,
                                                              @NotNull SlotPredicate slotPredicate,
                                                              int start, int end, int step);

    default @NotNull Pair<ItemStack, Map<Integer, ItemStack>> process(@NotNull AbstractInventory inventory,
                                                                      @NotNull ItemStack itemStack,
                                                                      @NotNull SlotPredicate slotPredicate) {
        return process(inventory, itemStack, slotPredicate, 0, inventory.getInnerSize(), 1);
    }

    default @NotNull Pair<ItemStack, Map<Integer, ItemStack>> process(@NotNull AbstractInventory inventory,
                                                                      @NotNull ItemStack itemStack) {
        return process(inventory, itemStack, (slot, itemStack1) -> true);
    }

    @FunctionalInterface
    interface SlotPredicate {
        boolean test(int slot, @NotNull ItemStack itemStack);
    }
}
