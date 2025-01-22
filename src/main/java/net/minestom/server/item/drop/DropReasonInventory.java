package net.minestom.server.item.drop;

import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;

/**
 * The item was dropped from an inventory
 */
public final class DropReasonInventory extends DropReason {

    private final AbstractInventory inventory;

    public DropReasonInventory(@NotNull AbstractInventory inventory) {
        this.inventory = inventory;
    }

    public @NotNull AbstractInventory getInventory() {
        return inventory;
    }
}
