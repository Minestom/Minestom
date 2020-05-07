package net.minestom.server.event.player;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

public class PlayerSwapItemEvent extends CancellableEvent {

    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    public PlayerSwapItemEvent(ItemStack mainHandItem, ItemStack offHandItem) {
        this.mainHandItem = mainHandItem;
        this.offHandItem = offHandItem;
    }

    public ItemStack getMainHandItem() {
        return mainHandItem;
    }

    public void setMainHandItem(ItemStack mainHandItem) {
        this.mainHandItem = mainHandItem;
    }

    public ItemStack getOffHandItem() {
        return offHandItem;
    }

    public void setOffHandItem(ItemStack offHandItem) {
        this.offHandItem = offHandItem;
    }
}
