package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickProcessors;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.ClickUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called after {@link InventoryInteractEvent} and before {@link InventoryPostClickEvent}.
 */
public class InventoryClickEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final Player player;

    private final ClickType type;
    private final int slot;

    private boolean cancelled;

    public InventoryClickEvent(@NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                               @NotNull Player player, @NotNull ClickType type, int slot) {
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.player = player;

        this.type = type;
        this.slot = slot;
    }

    /**
     * Gets the player who is trying to click on the inventory.
     *
     * @return the player who clicked
     */
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return the click type of this click event
     */
    public @NotNull ClickType getType() {
        return type;
    }

    /**
     * @return the specific slot that was clicked
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return whether or not the slot from {@link #getSlot()} is in the player inventory (as opposed to the clicked
     *         inventory)
     */
    public boolean isInPlayerInventory() {
        return inventory instanceof PlayerInventory || (slot != -1 && slot >= inventory.getSize());
    }

    /**
     * Gets the item that was clicked for this event.
     * @see ClickUtils#getItem(Click.Info, Inventory, PlayerInventory)
     */
    public @NotNull ItemStack getClickedItem() {
        return (isInPlayerInventory() ? playerInventory : inventory).getItemStack(slot);
    }

    /**
     * Gets the player inventory that was involved with the click.
     *
     * @return the player inventory
     */
    public @NotNull PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
