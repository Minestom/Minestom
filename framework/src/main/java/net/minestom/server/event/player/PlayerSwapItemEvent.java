package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;

/**
 * Called when a player is trying to swap his main and off hand item.
 */
public class PlayerSwapItemEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private boolean cancelled;

    public PlayerSwapItemEvent(Player player, ItemStack mainHandItem, ItemStack offHandItem) {
        this.player = player;
        this.mainHandItem = mainHandItem;
        this.offHandItem = offHandItem;
    }

    /**
     * Gets the item which will be in player main hand after the event.
     *
     * @return the item in main hand
     */
    public ItemStack getMainHandItem() {
        return mainHandItem;
    }

    /**
     * Changes the item which will be in the player main hand.
     *
     * @param mainHandItem the main hand item
     */
    public void setMainHandItem(ItemStack mainHandItem) {
        this.mainHandItem = mainHandItem;
    }

    /**
     * Gets the item which will be in player off hand after the event.
     *
     * @return the item in off hand
     */
    public ItemStack getOffHandItem() {
        return offHandItem;
    }

    /**
     * Changes the item which will be in the player off hand.
     *
     * @param offHandItem the off hand item
     */
    public void setOffHandItem(ItemStack offHandItem) {
        this.offHandItem = offHandItem;
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
    public Player getPlayer() {
        return player;
    }
}
