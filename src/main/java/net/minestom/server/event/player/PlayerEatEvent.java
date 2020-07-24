package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

/**
 * Called when a player is finished eating
 */
public class PlayerEatEvent extends Event {

    private final Player player;
    private final ItemStack foodItem;

    public PlayerEatEvent(Player player, ItemStack foodItem) {
        this.player = player;
        this.foodItem = foodItem;
    }

    /**
     * Get the player who is finished eating
     *
     * @return the concerned player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the food item that has been eaten
     *
     * @return the food item
     */
    public ItemStack getFoodItem() {
        return foodItem;
    }
}
