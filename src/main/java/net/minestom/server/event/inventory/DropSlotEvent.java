package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ButtonType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DropSlotEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final boolean all;
    private final int slot;
    private final ItemStack droppedItem;

    private boolean cancelled;

    public DropSlotEvent(@NotNull Player player, @NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                         boolean all, int slot, @NotNull ItemStack droppedItem) {
        this.player = player;
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        this.slot = slot;
        this.all = all;
        this.droppedItem = droppedItem;
    }

    public boolean dropAll() {
        return all;
    }

    public int getSlot() {
        return slot;
    }

    public @NotNull ItemStack getDroppedItem() {
        return droppedItem;
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
