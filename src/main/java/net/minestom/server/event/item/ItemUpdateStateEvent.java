package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public class ItemUpdateStateEvent extends Event {

    private ItemStack itemStack;
    private Player.Hand hand;
    private boolean handAnimation;

    public ItemUpdateStateEvent(ItemStack itemStack, Player.Hand hand) {
        this.itemStack = itemStack;
        this.hand = hand;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Player.Hand getHand() {
        return hand;
    }

    public void setHandAnimation(boolean handAnimation) {
        this.handAnimation = handAnimation;
    }

    public boolean hasHandAnimation() {
        return handAnimation;
    }
}
