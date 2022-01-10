package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used when a {@link Player} finish the animation of an item.
 *
 * @see ItemAnimationType
 */
public class PlayerItemAnimationEvent implements PlayerEvent, EntityInstanceEvent, CancellableEvent {

    private final Player player;
    private final ItemAnimationType armAnimationType;

    private boolean cancelled;

    public PlayerItemAnimationEvent(@NotNull Player player, @NotNull ItemAnimationType armAnimationType) {
        this.player = player;
        this.armAnimationType = armAnimationType;
    }

    /**
     * Gets the animation.
     *
     * @return the animation
     */
    @NotNull
    public ItemAnimationType getArmAnimationType() {
        return armAnimationType;
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
