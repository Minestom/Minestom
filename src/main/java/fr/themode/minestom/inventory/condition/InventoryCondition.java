package fr.themode.minestom.inventory.condition;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.inventory.click.ClickType;

public interface InventoryCondition {

    void accept(Player player, int slot, ClickType clickType, InventoryConditionResult inventoryConditionResult);

}
