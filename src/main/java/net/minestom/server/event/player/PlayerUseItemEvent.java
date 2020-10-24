package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event when an item is used without clicking on a block.
 */
public class PlayerUseItemEvent extends CancellableEvent {

    private final Player player;
    private final Player.Hand hand;
    private final ItemStack itemStack;

    public PlayerUseItemEvent(@NotNull Player player, @NotNull Player.Hand hand, @NotNull ItemStack itemStack) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
    }

    /**
     * Gets the player who used an item.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets which hand the player used.
     *
     * @return the hand used
     */
    @NotNull
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Gets the item which have been used.
     *
     * @return the item
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }
}
