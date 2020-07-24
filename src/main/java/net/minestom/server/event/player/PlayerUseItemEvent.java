package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

/**
 * Event when an item is used without clicking a block
 */
public class PlayerUseItemEvent extends CancellableEvent {

    private final Player player;
    private final Player.Hand hand;
    private final ItemStack itemStack;

    public PlayerUseItemEvent(Player player, Player.Hand hand, ItemStack itemStack) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
    }

    /**
     * Get the player who used an item
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get which hand the player used
     *
     * @return the hand used
     */
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Get the item which have been used
     *
     * @return the item
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
}
