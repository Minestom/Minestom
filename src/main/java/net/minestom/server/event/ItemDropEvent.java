package net.minestom.server.event;

import net.minestom.server.item.ItemStack;

public class ItemDropEvent extends CancellableEvent {

    private ItemStack itemStack;

    public ItemDropEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
