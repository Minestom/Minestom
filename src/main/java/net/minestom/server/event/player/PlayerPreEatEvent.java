package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;

/**
 * Called before the PlayerEatEvent and can be used to change the eating time
 * or to cancel its processing, cancelling the event means that the player will
 * continue the animation indefinitely.
 */
public class PlayerPreEatEvent implements ItemEvent, PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final ItemStack foodItem;
    private final PlayerHand hand;
    private long eatingTime;

    private boolean cancelled;

    public PlayerPreEatEvent(Player player, ItemStack foodItem, PlayerHand hand, long eatingTime) {
        this.player = player;
        this.foodItem = foodItem;
        this.hand = hand;
        this.eatingTime = eatingTime;
    }

    /**
     * The food item which will be eaten.
     *
     * @return the food item
     * @deprecated use getItemStack() for the eaten item
     */
    @Deprecated
    public ItemStack getFoodItem() {
        return foodItem;
    }

    public PlayerHand getHand() {
        return hand;
    }

    /**
     * Gets the food eating time in ticks.
     *
     * @return the eating time
     */
    public long getEatingTime() {
        return eatingTime;
    }

    /**
     * Changes the food eating time.
     *
     * @param eatingTime the new eating time in ticks
     */
    public void setEatingTime(long eatingTime) {
        this.eatingTime = eatingTime;
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

    /**
     * The food item which will be eaten.
     *
     * @return the food item
     */
    @Override
    public ItemStack getItemStack() { return foodItem; }
}
