package net.minestom.server.event;

import net.minestom.server.item.ItemStack;

public class ItemUpdateStateEvent extends Event {

    private ItemStack itemStack;
    private boolean handAnimation;

    public ItemUpdateStateEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setHandAnimation(boolean handAnimation) {
        this.handAnimation = handAnimation;
    }

    public boolean hasHandAnimation() {
        return handAnimation;
    }
}
