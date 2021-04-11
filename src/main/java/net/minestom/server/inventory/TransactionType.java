package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a type of transaction that you can apply to an {@link AbstractInventory}.
 */
public interface TransactionType {

    /**
     * Adds an item to the inventory.
     * Can either take an air slot or be stacked.
     */
    TransactionType ADD = (inventory, itemStack) -> {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();

        final StackingRule stackingRule = itemStack.getStackingRule();

        // Check filled slot (not air)
        for (int i = 0; i < inventory.getInnerSize(); i++) {
            ItemStack inventoryItem = inventory.getItemStack(i);
            if (inventoryItem.isAir()) {
                continue;
            }
            if (stackingRule.canBeStacked(itemStack, inventoryItem)) {
                final int itemAmount = stackingRule.getAmount(inventoryItem);
                if (itemAmount == stackingRule.getMaxSize())
                    continue;
                final int itemStackAmount = stackingRule.getAmount(itemStack);
                final int totalAmount = itemStackAmount + itemAmount;
                if (!stackingRule.canApply(itemStack, totalAmount)) {
                    // Slot cannot accept the whole item, reduce amount to 'itemStack'
                    itemChangesMap.put(i, stackingRule.apply(inventoryItem, stackingRule.getMaxSize()));
                    itemStack = stackingRule.apply(itemStack, totalAmount - stackingRule.getMaxSize());
                } else {
                    // Slot can accept the whole item
                    itemChangesMap.put(i, stackingRule.apply(inventoryItem, totalAmount));
                    itemStack = stackingRule.apply(itemStack, 0);
                    break;
                }
            }
        }

        // Check air slot to fill
        for (int i = 0; i < inventory.getInnerSize(); i++) {
            ItemStack inventoryItem = inventory.getItemStack(i);
            if (!inventoryItem.isAir()) {
                continue;
            }
            // Fill the slot
            itemChangesMap.put(i, itemStack);
            itemStack = stackingRule.apply(itemStack, 0);
            break;
        }

        return Pair.of(itemStack, itemChangesMap);
    };

    /**
     * Takes an item from the inventory.
     * Can either transform items to air or reduce their amount.
     */
    TransactionType TAKE = (inventory, itemStack) -> {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();
        final StackingRule stackingRule = itemStack.getStackingRule();
        for (int i = 0; i < inventory.getInnerSize(); i++) {
            ItemStack inventoryItem = inventory.getItemStack(i);
            if (inventoryItem.isAir()) {
                continue;
            }
            if (stackingRule.canBeStacked(itemStack, inventoryItem)) {
                final int itemAmount = stackingRule.getAmount(inventoryItem);
                final int itemStackAmount = stackingRule.getAmount(itemStack);
                if (itemStackAmount < itemAmount) {
                    itemChangesMap.put(i, stackingRule.apply(inventoryItem, itemAmount - itemStackAmount));
                    itemStack = stackingRule.apply(itemStack, 0);
                    break;
                }
                itemChangesMap.put(i, stackingRule.apply(inventoryItem, 0));
                itemStack = stackingRule.apply(itemStack, itemStackAmount - itemAmount);
                if (stackingRule.getAmount(itemStack) == 0) {
                    itemStack = stackingRule.apply(itemStack, 0);
                    break;
                }
            }
        }

        return Pair.of(itemStack, itemChangesMap);
    };

    @NotNull Pair<ItemStack, Map<Integer, ItemStack>> process(@NotNull AbstractInventory inventory,
                                                              @NotNull ItemStack itemStack);

}
