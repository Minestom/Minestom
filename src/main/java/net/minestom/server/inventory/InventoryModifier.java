package net.minestom.server.inventory;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an inventory where items can be modified/retrieved.
 */
public interface InventoryModifier {

    /**
     * Sets an {@link ItemStack} at the specified slot and send relevant update to the viewer(s).
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    void setItemStack(int slot, @NotNull ItemStack itemStack);

    /**
     * Adds an {@link ItemStack} to the inventory and send relevant update to the viewer(s).
     * <p>
     * Even the item cannot be fully added, the amount of {@code itemStack} will be updated.
     *
     * @param itemStack the item to add
     * @return true if the item has been successfully fully added, false otherwise
     */
    default boolean addItemStack(@NotNull ItemStack itemStack) {
        return addItemStack(itemStack, 0, getSize());
    }

    default boolean addItemStack(@NotNull ItemStack itemStack, int startSlot, int endSlot) {
        Int2ObjectMap<ItemStack> itemChangesMap = new Int2ObjectOpenHashMap<>();

        final StackingRule stackingRule = itemStack.getStackingRule();
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack inventoryItem = getItemStack(i);
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
                    itemChangesMap.put(i, inventoryItem.withAmount(totalAmount));
                    itemStack = ItemStack.AIR;
                    break;
                }
            } else if (inventoryItem.isAir()) {
                // Fill the slot
                itemChangesMap.put(i, itemStack);
                itemStack = ItemStack.AIR;
                break;
            }
        }

        if (itemStack.isAir()) {
            // Item can be fully placed inside the inventory, do so
            itemChangesMap.forEach(this::setItemStack);
            return true;
        } else {
            // Inventory cannot accept the item fully
            return false;
        }
    }

    /**
     * Clears the inventory and send relevant update to the viewer(s).
     */
    void clear();

    /**
     * Gets the {@link ItemStack} at the specified slot.
     *
     * @param slot the slot to check
     * @return the item in the slot {@code slot}
     */
    @NotNull ItemStack getItemStack(int slot);

    /**
     * Gets all the {@link ItemStack} in the inventory.
     * <p>
     * Be aware that the returned array does not need to be the original one,
     * meaning that modifying it directly may not work.
     *
     * @return an array containing all the inventory's items
     */
    @NotNull ItemStack[] getItemStacks();

    /**
     * Gets the size of the inventory.
     *
     * @return the inventory's size
     */
    int getSize();

    /**
     * Gets all the {@link InventoryCondition} of this inventory.
     *
     * @return a modifiable {@link List} containing all the inventory conditions
     */
    @NotNull List<InventoryCondition> getInventoryConditions();

    /**
     * Adds a new {@link InventoryCondition} to this inventory.
     *
     * @param inventoryCondition the inventory condition to add
     */
    void addInventoryCondition(@NotNull InventoryCondition inventoryCondition);

    /**
     * Places all the items of {@code itemStacks} into the internal array.
     *
     * @param itemStacks the array to copy the content from
     * @throws IllegalArgumentException if the size of the array is not equal to {@link #getSize()}
     * @throws NullPointerException     if {@code itemStacks} contains one null element or more
     */
    default void copyContents(@NotNull ItemStack[] itemStacks) {
        Check.argCondition(itemStacks.length != getSize(),
                "The size of the array has to be of the same size as the inventory: " + getSize());

        for (int i = 0; i < itemStacks.length; i++) {
            final ItemStack itemStack = itemStacks[i];
            Check.notNull(itemStack, "The item array cannot contain any null element!");
            setItemStack(i, itemStack);
        }
    }
}
