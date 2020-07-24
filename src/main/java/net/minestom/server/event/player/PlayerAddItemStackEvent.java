package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.item.ItemStackUtils;

/**
 * Called as a result of {@link net.minestom.server.inventory.PlayerInventory#addItemStack(ItemStack)}
 */
public class PlayerAddItemStackEvent extends CancellableEvent {

    private final Player player;
    private ItemStack itemStack;

    public PlayerAddItemStackEvent(Player player, ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    /**
     * Get the player who has an item stack added to his inventory
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the item stack which will be added
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Change the item stack which will be added
     *
     * @param itemStack the new item stack
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = ItemStackUtils.notNull(itemStack);
    }
}
