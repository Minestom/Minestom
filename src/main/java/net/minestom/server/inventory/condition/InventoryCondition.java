package net.minestom.server.inventory.condition;

import net.minestom.server.entity.Player;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.ClickInfo;
import net.minestom.server.inventory.click.ClickResult;
import org.jetbrains.annotations.Nullable;

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
     * @param result                   the result of the click
     * @return                         the new result of this click, null to cancel
     */
    @Nullable ClickResult accept(Player player, ClickInfo clickInfo, @Nullable ClickResult result);
}
