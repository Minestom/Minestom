package fr.themode.minestom.event;

import fr.themode.minestom.item.ItemStack;

public class PickupItemEvent extends CancellableEvent {

    private ItemStack itemStack;

    public PickupItemEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
