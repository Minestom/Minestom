package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event when an item is used without clicking on a block.
 */
public class PlayerUseItemEvent implements PlayerInstanceEvent, ItemEvent, CancellableEvent {

    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;

    private long itemUseTime;
    private boolean cancelled;

    public PlayerUseItemEvent(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long itemUseTime) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
        this.itemUseTime = itemUseTime;
    }

    /**
     * Gets which hand the player used.
     *
     * @return the hand used
     */
    public @NotNull PlayerHand getHand() {
        return hand;
    }

    /**
     * Gets the item which has been used.
     *
     * @return the item
     */
    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the item usage duration. After this amount of milliseconds,
     * the animation will stop automatically and {@link ItemUpdateStateEvent} is called.
     *
     * @return the item use time
     */
    public long getItemUseTime() {
        return itemUseTime;
    }

    /**
     * Changes the item usage duration.
     *
     * @param itemUseTime the new item use time
     */
    public void setItemUseTime(long itemUseTime) {
        this.itemUseTime = itemUseTime;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
