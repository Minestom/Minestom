package fr.themode.minestom.inventory.rule;

import fr.themode.minestom.inventory.Inventory;

public interface InventoryCondition {

    void accept(int slot, Inventory inventory, InventoryConditionResult inventoryConditionResult);

}
