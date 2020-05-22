package net.minestom.server.inventory;

import net.minestom.server.inventory.condition.InventoryCondition;
import net.minestom.server.item.ItemStack;

import java.util.List;

public interface InventoryModifier {

    void setItemStack(int slot, ItemStack itemStack);

    boolean addItemStack(ItemStack itemStack);

    ItemStack getItemStack(int slot);

    ItemStack[] getItemStacks();

    int getSize();

    List<InventoryCondition> getInventoryConditions();

    void addInventoryCondition(InventoryCondition inventoryCondition);
}
