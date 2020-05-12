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

    private Player player;
    private ItemStack foodItem;
    private long eatingTime;

    public PlayerPreEatEvent(Player player, ItemStack foodItem, long eatingTime) {
        this.player = player;
        this.foodItem = foodItem;
        this.eatingTime = eatingTime;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getFoodItem() {
        return foodItem;
    }

    public long getEatingTime() {
        return eatingTime;
    }

    public void setEatingTime(long eatingTime) {
        this.eatingTime = eatingTime;
    }
}
