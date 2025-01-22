package net.minestom.server.item.drop;

import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;

public class DropReason {

    /**
     * Creates a new DropReason from an inventory
     * @param inventory The inventory the item was dropped from
     */
    public static DropReason fromInventory(@NotNull AbstractInventory inventory) {
        return new DropReasonInventory(inventory);
    }

    /**
     * Creates a new DropReason from an inventory closing
     */
    public static DropReason fromInventoryClose() {
        return new DropReasonClose();
    }

    /**
     * Creates a new DropReason from the player's currently held hotbar slot
     * @param slot The slot the item was dropped from
     */
    public static DropReason fromHotbar(int slot) {
        return new DropReasonHotbar(slot);
    }
}
