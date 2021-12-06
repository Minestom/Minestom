package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

public interface PlayerSnapshot extends EntitySnapshot {
    @NotNull InventorySnapshot inventory();

    @NotNull InventorySnapshot openInventory();
}
