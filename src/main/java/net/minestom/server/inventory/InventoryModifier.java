package net.minestom.server.inventory;

import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an inventory where its items can be modified/retrieved.
 */
public interface InventoryModifier {

    /**
     * Sets an {@link ItemStack} at the specified slot.
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    void setItemStack(int slot, @NotNull ItemStack itemStack);

    /**
     * Adds an {@link ItemStack} to the inventory.
     *
     * @param itemStack the item to add
     * @return true if the item has been successfully fully added, false otherwise
     */
    boolean addItemStack(@NotNull ItemStack itemStack);

    /**
     * Clears the inventory.
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
     * @return the inventory conditions
     */
    @NotNull
    List<InventoryCondition> getInventoryConditions();

    /**
     * Adds a new {@link InventoryCondition} to this inventory.
     *
     * @param inventoryCondition the inventory condition to add
     */
    void addInventoryCondition(@NotNull InventoryCondition inventoryCondition);
}
