package net.minestom.server.inventory.condition;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.InventoryModifier;
import net.minestom.server.inventory.click.ClickType;

/**
 * Can be added to any {@link InventoryModifier} in order to listen to any issued clicks.
 */
@FunctionalInterface
public interface InventoryCondition {

    /**
     * Called when a {@link Player} clicks in the inventory where this {@link InventoryCondition} has been added to.
     *
     * @param player                   the player who clicked in the inventory
     * @param slot                     the slot clicked
     * @param clickType                the click type
     * @param inventoryConditionResult the result of this callback
     */
    void accept(Player player, int slot, ClickType clickType, InventoryConditionResult inventoryConditionResult);
}
