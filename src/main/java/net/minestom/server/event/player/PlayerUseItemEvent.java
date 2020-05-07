package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

/**
 * Event when an item is used without clicking a block
 */
public class PlayerUseItemEvent extends CancellableEvent {

    private Player.Hand hand;
    private ItemStack itemStack;

    public PlayerUseItemEvent(Player.Hand hand, ItemStack itemStack) {
        this.hand = hand;
        this.itemStack = itemStack;
    }

    public Player.Hand getHand() {
        return hand;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
