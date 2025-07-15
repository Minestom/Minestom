package net.minestom.server.event.inventory;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;

/**
 * Called when a player interacts with an item in the creative menu
 */
public class CreativeInventoryActionEvent implements PlayerInstanceEvent, CancellableEvent {
    private final Player player;
    private final int slot;
    private ItemStack clickedItem;
    private boolean cancelled;

    public CreativeInventoryActionEvent(Player player,
                                        int slot,
                                        ItemStack clicked) {
        this.player = player;
        this.slot = slot;
        this.clickedItem = clicked;
        this.cancelled = false;
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
     * Gets the clicked slot number.
     *
     * @return the clicked slot number
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Gets the item which has been clicked.
     *
     * @return the clicked item
     */
    public ItemStack getClickedItem() {
        return clickedItem;
    }

    /**
     * Changes the clicked item.
     *
     * @param clickedItem the clicked item
     */
    public void setClickedItem(ItemStack clickedItem) {
        this.clickedItem = clickedItem;
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
