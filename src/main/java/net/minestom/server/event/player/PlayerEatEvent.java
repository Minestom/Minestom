package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is finished eating.
 */
public class PlayerEatEvent extends PlayerEvent {

    private final ItemStack foodItem;
    private final Player.Hand hand;

    public PlayerEatEvent(@NotNull Player player, @NotNull ItemStack foodItem, @NotNull Player.Hand hand) {
        super(player);
        this.foodItem = foodItem;
        this.hand = hand;
    }

    /**
     * Gets the food item that has been eaten.
     *
     * @return the food item
     */
    public @NotNull ItemStack getFoodItem() {
        return foodItem;
    }

    public @NotNull Player.Hand getHand() {
        return hand;
    }
}
