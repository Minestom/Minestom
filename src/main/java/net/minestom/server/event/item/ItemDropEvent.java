package net.minestom.server.event.item;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

public class ItemDropEvent extends CancellableEvent {

    private final ItemStack itemStack;

    public ItemDropEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
