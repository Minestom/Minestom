package net.minestom.server.item.drop;

import net.minestom.server.inventory.AbstractInventory;
import org.jetbrains.annotations.NotNull;

public class DropReason {

    public static DropReason fromInventory(@NotNull AbstractInventory inventory) {
        return new DropReasonInventory(inventory);
    }
    public static DropReason fromInventoryClose() {
        return new DropReasonClose();
    }
    public static DropReason fromHotbar(int slot) {
        return new DropReasonHotbar(slot);
    }
}
