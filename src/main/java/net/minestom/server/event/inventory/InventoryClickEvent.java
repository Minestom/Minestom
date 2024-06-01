package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickProcessors;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.ClickUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called after {@link InventoryPreClickEvent} and before {@link InventoryPostClickEvent}.
 */
public class InventoryClickEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final Player player;
    private final Click.Info info;
    private List<Click.Change> changes;

    private boolean cancelled;

    public InventoryClickEvent(@NotNull PlayerInventory playerInventory, @NotNull Inventory inventory,
                               @NotNull Player player, @NotNull Click.Info info, @NotNull List<Click.Change> changes) {
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.player = player;
        this.info = info;
        this.changes = changes;
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
     * Gets the info about the click that occurred. This is enough to fully describe the click.
     *
     * @return the click info
     */
    public @NotNull Click.Info getClickInfo() {
        return info;
    }

    /**
     * Gets the changes that will occur as a result of this click.
     *
     * @return the changes
     */
    public @NotNull List<Click.Change> getChanges() {
        return changes;
    }

    /**
     * Updates the changes that will occur as a result of this click.
     *
     * @param changes the new results
     */
    public void setChanges(@NotNull List<Click.Change> changes) {
        this.changes = changes;
    }

    /**
     * Updates the changes that will occur as a result of this click to the changes supplied by the provided
     * {@link ClickProcessors.InventoryProcessor}.
     * @param processor the processor that will immediately be used to supply changes
     */
    public void useClickProcessor(@NotNull ClickProcessors.InventoryProcessor processor) {
        setChanges(processor.apply(info, ClickUtils.makeGetter(inventory, playerInventory)));
    }

    /**
     * @return the click type of this click event
     * @see ClickUtils#getType(Click.Info)
     */
    public @NotNull Click.Type getType() {
        return ClickUtils.getType(info);
    }

    /**
     * @return whether or not the slot from {@link #getSlot()} is in the player inventory (as opposed to the clicked
     *         inventory)
     */
    public boolean isInPlayerInventory() {
        int raw = ClickUtils.getSlot(info);
        return inventory instanceof PlayerInventory || (raw != -1 && raw >= inventory.getSize());
    }

    /**
     * @return the slot that was clicked
     * @see ClickUtils#getSlot(Click.Info)
     */
    public int getSlot() {
        return PlayerInventoryUtils.protocolToMinestom(ClickUtils.getSlot(info), inventory.getSize());
    }

    /**
     * Gets the item that was clicked for this event.
     * @see ClickUtils#getItem(Click.Info, Inventory, PlayerInventory)
     */
    public @NotNull ItemStack getClickedItem() {
        return ClickUtils.getItem(info, inventory, playerInventory);
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
