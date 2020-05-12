package net.minestom.server.event.player;

import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public class PlayerEatEvent extends Event {

    private ItemStack foodItem;

    public PlayerEatEvent(ItemStack foodItem) {
        this.foodItem = foodItem;
    }

    public ItemStack getFoodItem() {
        return foodItem;
    }
}
