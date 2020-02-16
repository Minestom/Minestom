package fr.themode.minestom.inventory.rule;

import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.item.ItemStack;

public interface InventoryCondition {

    InventoryConditionResult accept(int slot, Inventory inventory, ItemStack clickedItem, ItemStack cursorItem);

}
