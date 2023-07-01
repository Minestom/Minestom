package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used when a {@link Player} finish the animation of an item.
 *
 * @see ItemAnimationType
 */
public class PlayerItemAnimationEvent implements PlayerInstanceEvent, CancellableEvent {

    private final Player player;
    private final ItemAnimationType itemAnimationType;
    private final Player.Hand hand;
    private boolean cancelled;

    public PlayerItemAnimationEvent(@NotNull Player player, @NotNull ItemAnimationType itemAnimationType, @NotNull Player.Hand hand) {
        this.player = player;
        this.itemAnimationType = itemAnimationType;
        this.hand = hand;
    }

    /**
     * Gets the animation.
     *
     * @return the animation
     */
    public @NotNull ItemAnimationType getItemAnimationType() {
        return itemAnimationType;
    }

    /**
     * Gets the hand that was used.
     *
     * @return the hand
     */
    public @NotNull Player.Hand getHand() {
        return hand;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    public enum ItemAnimationType {
        BOW,
        CROSSBOW,
        TRIDENT,
        SHIELD,
        EAT
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
