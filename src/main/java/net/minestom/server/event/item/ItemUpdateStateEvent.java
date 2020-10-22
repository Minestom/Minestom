package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;

public class ItemUpdateStateEvent extends Event {

    private final Player player;
    private final Player.Hand hand;
    private final ItemStack itemStack;
    private boolean handAnimation;

    public ItemUpdateStateEvent(Player player, Player.Hand hand, ItemStack itemStack) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
    }

    public Player getPlayer() {
        return player;
    }

    public Player.Hand getHand() {
        return hand;
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
