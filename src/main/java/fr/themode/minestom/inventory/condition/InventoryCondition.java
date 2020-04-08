package fr.themode.minestom.inventory.condition;

import fr.themode.minestom.entity.Player;

public interface InventoryCondition {

    void accept(Player player, int slot, InventoryConditionResult inventoryConditionResult);

}
