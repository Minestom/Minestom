package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player is trying to swap his main and off hand item.
 */
public class PlayerSwapItemEvent extends CancellableEvent {

    private final Player player;
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    public PlayerSwapItemEvent(@NotNull Player player, @NotNull ItemStack mainHandItem, @NotNull ItemStack offHandItem) {
        this.player = player;
        this.mainHandItem = mainHandItem;
        this.offHandItem = offHandItem;
    }

    /**
     * Gets the player who is trying to swap his hands item.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item which will be in player main hand after the event.
     *
     * @return the item in main hand
     */
    @NotNull
    public ItemStack getMainHandItem() {
        return mainHandItem;
    }

    /**
     * Changes the item which will be in the player main hand.
     *
     * @param mainHandItem the main hand item
     */
    public void setMainHandItem(@NotNull ItemStack mainHandItem) {
        this.mainHandItem = mainHandItem;
    }

    /**
     * Gets the item which will be in player off hand after the event.
     *
     * @return the item in off hand
     */
    @NotNull
    public ItemStack getOffHandItem() {
        return offHandItem;
    }

    /**
     * Changes the item which will be in the player off hand.
     *
     * @param offHandItem the off hand item
     */
    public void setOffHandItem(@NotNull ItemStack offHandItem) {
        this.offHandItem = offHandItem;
    }
}
