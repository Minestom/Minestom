package fr.themode.minestom.inventory;

import fr.themode.minestom.item.ItemStack;

public interface InventoryModifier {

    void setItemStack(int slot, ItemStack itemStack);

    boolean addItemStack(ItemStack itemStack);

    ItemStack getItemStack(int slot);

    ItemStack[] getItemStacks();
}
