package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;

/**
 * Called before {@link InventoryClickEvent}, used to potentially cancel the click.
 */
public class InventoryPreClickEvent implements InventoryEvent, PlayerInstanceEvent, CancellableEvent {

    private final AbstractInventory inventory;
    private final Player player;
    private Click click;

    private boolean cancelled;

    public InventoryPreClickEvent(AbstractInventory inventory,
                                  Player player,
                                  Click click) {
        this.inventory = inventory;
        this.player = player;
        this.click = click;
    }

    /**
     * Gets the player who is trying to click on the inventory.
     *
     * @return the player who clicked
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the player's click.
     */
    public Click getClick() {
        return click;
    }

    /**
     * Sets the player's click.
     */
    public void setClick(Click click) {
        this.click = click;
    }

    /**
     * Returns the clicked slot. This is only for convenience and may return -999 (a meaningless number), as some clicks
     * don't have a relevant slot (drag clicks and some drops). See {@link Click#slot()} for details.
     */
    public int getSlot() {
        return this.click.slot();
    }

    /**
     * Returns the clicked item. Some clicks involve more than a single item, like drops or clicks outside the inventory
     * menu; in these cases, the cursor is returned.
     */
    public ItemStack getClickedItem() {
        int slot = getSlot();

        return slot == -999 ? player.getInventory().getCursorItem()
                : this.inventory.getItemStack(slot);
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
    public AbstractInventory getInventory() {
        return inventory;
    }
}
