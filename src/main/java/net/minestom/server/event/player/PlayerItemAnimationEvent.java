package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;

/**
 * Used when the player finish the animation of an item
 *
 * @see ItemAnimationType
 */
public class PlayerItemAnimationEvent extends CancellableEvent {

    private final Player player;
    private final ItemAnimationType armAnimationType;

    public PlayerItemAnimationEvent(Player player, ItemAnimationType armAnimationType) {
        this.player = player;
        this.armAnimationType = armAnimationType;
    }

    /**
     * Get the player who is responsive for the animation
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the animation
     *
     * @return the animation
     */
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
