package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

/**
 * Called before the PlayerEatEvent and can be used to change the eating time
 * or to cancel its processing, cancelling the event means that the player will
 * continue the animation indefinitely
 */
public class PlayerPreEatEvent extends CancellableEvent {

    private final Player player;
    private final ItemStack foodItem;
    private long eatingTime;

    public PlayerPreEatEvent(Player player, ItemStack foodItem, long eatingTime) {
        this.player = player;
        this.foodItem = foodItem;
        this.eatingTime = eatingTime;
    }

    /**
     * The player who is trying to eat
     *
     * @return the concerned player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The food item which will be eaten
     *
     * @return the food item
     */
    public ItemStack getFoodItem() {
        return foodItem;
    }

    /**
     * Get the food eating time
     * <p>
     * This is by default {@link Player#getDefaultEatingTime()}
     *
     * @return the eating time
     */
    public long getEatingTime() {
        return eatingTime;
    }

    /**
     * Change the food eating time
     *
     * @param eatingTime the new eating time
     */
    public void setEatingTime(long eatingTime) {
        this.eatingTime = eatingTime;
    }
}
