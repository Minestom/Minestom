package net.minestom.server.inventory.condition;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.ClickInfo;

/**
 * Can be added to any {@link AbstractInventory}
 * using {@link net.minestom.server.inventory.Inventory#addInventoryCondition(InventoryCondition)}
 * or {@link net.minestom.server.inventory.PlayerInventory#addInventoryCondition(InventoryCondition)}
 * in order to listen to any issued clicks.
 */
@FunctionalInterface
public interface InventoryCondition {

    /**
     * Called when a {@link Player} clicks in the inventory where this {@link InventoryCondition} has been added to.
     *
     * @param player                   the player who clicked in the inventory
     * @param clickInfo                the click info
     * @param inventoryConditionResult the result of this callback
     */
    void accept(Player player, ClickInfo clickInfo, InventoryConditionResult inventoryConditionResult);
}
