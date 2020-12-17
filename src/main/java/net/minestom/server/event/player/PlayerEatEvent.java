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

    public PlayerEatEvent(@NotNull Player player, @NotNull ItemStack foodItem) {
        super(player);
        this.foodItem = foodItem;
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
