package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeInventoryActionEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final int slot;
    private final ItemStack item;

    private boolean cancelled;

    public CreativeInventoryActionEvent(@NotNull Player player, @NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                                        int slot, @NotNull ItemStack item) {
        this.player = player;
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        this.slot = slot;
        this.item = item;
    }

    /**
     * @return the interacted slot. -1 indicates the item is being dropped.
     */
    public int getSlot() {
        return slot;
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public @NotNull PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}

