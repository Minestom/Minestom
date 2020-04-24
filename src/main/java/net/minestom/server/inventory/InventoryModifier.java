package net.minestom.server.inventory;

import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;

public interface InventoryModifier {

    void setItemStack(int slot, ItemStack itemStack);

    boolean addItemStack(ItemStack itemStack);

    ItemStack getItemStack(int slot);

    ItemStack[] getItemStacks();

    InventoryCondition getInventoryCondition();

    void setInventoryCondition(InventoryCondition inventoryCondition);
}
