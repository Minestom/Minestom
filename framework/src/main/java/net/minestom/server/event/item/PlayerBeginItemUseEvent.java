package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.validate.Check;

/**
 * Called when a player begins using an item with the item, animation, and duration.
 *
 * <p>Setting the use duration to zero or cancelling the event will prevent consumption.</p>
 */
public class PlayerBeginItemUseEvent implements PlayerInstanceEvent, ItemEvent, CancellableEvent {
    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;
    private final ItemAnimation animation;
    private long itemUseDuration;

    private boolean cancelled = false;

    public PlayerBeginItemUseEvent(Player player, PlayerHand hand,
                                   ItemStack itemStack, ItemAnimation animation,
                                   long itemUseDuration) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
        this.animation = animation;
        this.itemUseDuration = itemUseDuration;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public PlayerHand getHand() {
        return hand;
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemAnimation getAnimation() {
        return animation;
    }

    /**
     * Returns the item use duration, in ticks. A duration of zero will prevent consumption (same effect as cancellation).
     *
     * @return the current item use duration
     */
    public long getItemUseDuration() {
        return itemUseDuration;
    }

    /**
     * Sets the item use duration, in ticks.
     */
    public void setItemUseDuration(long itemUseDuration) {
        Check.argCondition(itemUseDuration < 0, "Item use duration cannot be negative");
        this.itemUseDuration = itemUseDuration;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
