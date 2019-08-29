package fr.themode.minestom.event;

import fr.themode.minestom.item.ItemStack;

public class ItemDropEvent extends CancellableEvent {

    private ItemStack itemStack;

    public ItemDropEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
