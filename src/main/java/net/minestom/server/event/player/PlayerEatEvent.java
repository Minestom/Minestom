package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is finished eating.
 */
public class PlayerEatEvent extends Event {

    private final Player player;
    private final ItemStack foodItem;

    public PlayerEatEvent(@NotNull Player player, @NotNull ItemStack foodItem) {
        this.player = player;
        this.foodItem = foodItem;
    }

    /**
     * Gets the player who is finished eating.
     *
     * @return the concerned player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the food item that has been eaten.
     *
     * @return the food item
     */
    @NotNull
    public ItemStack getFoodItem() {
        return foodItem;
    }
}
