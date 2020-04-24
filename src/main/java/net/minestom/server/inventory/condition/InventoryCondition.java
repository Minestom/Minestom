package net.minestom.server.inventory.condition;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.click.ClickType;

public interface InventoryCondition {

    void accept(Player player, int slot, ClickType clickType, InventoryConditionResult inventoryConditionResult);

}
