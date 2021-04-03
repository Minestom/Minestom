package net.minestom.server.inventory;

import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
     * Adds an {@link ItemStack} to the inventory and sends relevant update to the viewer(s).
     * <p>
     * Even the item cannot be fully added, the amount of {@code itemStack} will be updated.
     *
     * @param itemStack the item to add
     * @return true if the item has been successfully fully added, false otherwise
     */
    boolean addItemStack(@NotNull ItemStack itemStack);

    /**
     * Adds {@link ItemStack}s to the inventory and sends relevant updates to the viewer(s).
     * <p>
     * Even items cannot be fully added, the amount of {@code itemStack}s will be updated.
     *
     * @param itemStacks items to add
     * @return list of itemstacks that could not be successfully fully added, empty list otherwise
     */
    default List<ItemStack> addItemStacks(@NotNull List<ItemStack> itemStacks) {
        List<ItemStack> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(itemStack -> {
            if (!addItemStack(itemStack)) {
                result.add(itemStack);
            }
        });
        return result;
    }

    /**
     * Checks whether {@link ItemStack} can be fully added to the inventory.
     *
     * @param itemStack the item to be checked
     * @return true if the item can be fully added to the inventory, false otherwise
     */
    boolean canAddItemStack(@NotNull ItemStack itemStack);

    /**
     * Checks whether {@link ItemStack}s can be fully added to the inventory.
     *
     * @param itemStacks items to be checked
     * @return true if all the items can be fully added to the inventory, false otherwise
     */
    default boolean canAddItemStacks(@NotNull List<ItemStack> itemStacks) {
        return itemStacks.stream().allMatch(this::canAddItemStack);
    }

    /**
     * Takes an {@link ItemStack} from the inventory and sends relevant update to the viewer(s).
     * <p>
     * Even the item cannot be fully taken, the amount of {@code itemStack} will be updated.
     *
     * @param itemStack the item to take
     * @return true if the item has been successfully fully taken, false otherwise
     */
    boolean takeItemStack(@NotNull ItemStack itemStack);

    /**
     * Takes {@link ItemStack}s from the inventory and sends relevant updates to the viewer(s).
     * <p>
     * Even items cannot be fully taken, the amount of {@code itemStack}s will be updated.
     *
     * @param itemStacks items to take
     * @return list of itemstacks that could not be successfully fully taken, empty list otherwise
     */
    default List<ItemStack> takeItemStacks(@NotNull List<ItemStack> itemStacks) {
        List<ItemStack> result = new ArrayList<>(itemStacks.size());
        itemStacks.forEach(itemStack -> {
            if (!takeItemStack(itemStack)) {
                result.add(itemStack);
            }
        });
        return result;
    }

    /**
     * Checks whether {@link ItemStack} can be fully taken from the inventory.
     *
     * @param itemStack the item to be checked
     * @return true if the item can be fully taken from the inventory, false otherwise
     */
    boolean canTakeItemStack(@NotNull ItemStack itemStack);

    /**
     * Checks whether {@link ItemStack}s can be fully taken from the inventory.
     *
     * @param itemStacks items to be checked
     * @return true if all the items can be fully taken from the inventory, false otherwise
     */
    default boolean canTakeItemStacks(@NotNull List<ItemStack> itemStacks) {
        return itemStacks.stream().allMatch(this::canTakeItemStack);
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
    @NotNull
    ItemStack getItemStack(int slot);

    /**
     * Gets all the {@link ItemStack} in the inventory.
     * <p>
     * Be aware that the returned array does not need to be the original one,
     * meaning that modifying it directly may not work.
     *
     * @return an array containing all the inventory's items
     */
    @NotNull
    ItemStack[] getItemStacks();

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
    @NotNull
    List<InventoryCondition> getInventoryConditions();

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
