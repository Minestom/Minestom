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

import java.util.Map;

public class InventoryDragEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final ButtonType initialButtonType;
    private final ItemStack distributedItem;
    private final Map<Integer, Integer> distributedAmounts;

    private boolean cancelled;

    public InventoryDragEvent(@NotNull Player player, @NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                              @NotNull ItemStack distributedItem, @NotNull ButtonType initialButtonType, @NotNull Map<Integer, Integer> distributedAmounts) {
        this.player = player;
        this.playerInventory = playerInventory;
        this.inventory = inventory;

        this.distributedItem = distributedItem;
        this.initialButtonType = initialButtonType;
        this.distributedAmounts = distributedAmounts;
    }

    public ButtonType getInitialButtonType() {
        return initialButtonType;
    }

    public @NotNull ItemStack getDistributedItem() {
        return distributedItem;
    }

    public Map<Integer, Integer> getDistributedAmounts() {
        return distributedAmounts;
    }

    public int getDistributedAmount(int slot) {
        return distributedAmounts.getOrDefault(slot, 0);
    }

    public void setDistributedAmount(int slot, int amount) {
        distributedAmounts.put(slot, amount);
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
