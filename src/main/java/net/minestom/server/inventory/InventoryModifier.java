package net.minestom.server.inventory;

import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;

import java.util.List;

/**
 * Represent an inventory where its items can be modified/retrieved
 */
public interface InventoryModifier {

    /**
     * Set an item at the specified slot
     *
     * @param slot      the slot to set the item
     * @param itemStack the item to set
     */
    void setItemStack(int slot, ItemStack itemStack);

    /**
     * Add an item to the inventory
     *
     * @param itemStack the item to add
     * @return true if the item has been sucessfully fully added, false otherwise
     */
    boolean addItemStack(ItemStack itemStack);

    /**
     * Clear the inventory
     */
    void clear();

    /**
     * Get the item at the specified slot
     *
     * @param slot the slot to check
     * @return the item in the slot {@code slot}
     */
    ItemStack getItemStack(int slot);

    /**
     * Get all the items in the inventory
     *
     * @return an array containing all the inventory's items
     */
    ItemStack[] getItemStacks();

    /**
     * Get the size of the inventory
     *
     * @return the inventory's size
     */
    int getSize();

    /**
     * Get all the inventory conditions of this inventory
     *
     * @return the inventory conditions
     */
    List<InventoryCondition> getInventoryConditions();

    /**
     * Add a new inventory condition to this inventory
     *
     * @param inventoryCondition the inventory condition to add
     */
    void addInventoryCondition(InventoryCondition inventoryCondition);
}
