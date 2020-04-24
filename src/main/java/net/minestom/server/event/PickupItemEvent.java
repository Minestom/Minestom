package net.minestom.server.event;

import net.minestom.server.item.ItemStack;

public class PickupItemEvent extends CancellableEvent {

    private ItemStack itemStack;

    public PickupItemEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
