package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used when a {@link Player} finish the animation of an item.
 *
 * @see ItemAnimationType
 */
public class PlayerItemAnimationEvent extends CancellableEvent {

    private final Player player;
    private final ItemAnimationType armAnimationType;

    public PlayerItemAnimationEvent(@NotNull Player player, @NotNull ItemAnimationType armAnimationType) {
        this.player = player;
        this.armAnimationType = armAnimationType;
    }

    /**
     * Gets the {@link Player} who is responsible for the animation.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
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

    public enum ItemAnimationType {
        BOW,
        CROSSBOW,
        TRIDENT,
        SHIELD,
        EAT
    }

}
