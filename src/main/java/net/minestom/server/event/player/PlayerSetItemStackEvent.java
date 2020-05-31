package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;

/**
 * Called as a result of {@link net.minestom.server.inventory.PlayerInventory#setItemStack(int, ItemStack)}
 */
public class PlayerSetItemStackEvent extends CancellableEvent {

    private Player player;
    private ItemStack itemStack;

    public PlayerSetItemStackEvent(Player player, ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    /**
     * Get the player who has an item stack set to his inventory
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the item stack which will be set
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Change the item stack which will be set
     *
     * @param itemStack the new item stack
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
