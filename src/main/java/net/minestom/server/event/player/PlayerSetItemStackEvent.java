package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called as a result of {@link net.minestom.server.inventory.PlayerInventory#setItemStack(int, ItemStack)}
 * and player click in his inventory.
 */
public class PlayerSetItemStackEvent extends CancellableEvent {

    private final Player player;
    private int slot;
    private ItemStack itemStack;

    public PlayerSetItemStackEvent(@NotNull Player player, int slot, @NotNull ItemStack itemStack) {
        this.player = player;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    /**
     * Gets the player who has an item stack set to his inventory.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the slot where the item will be set.
     *
     * @return the slot
     */
    public int getSlot() {
        return slot;
    }

    /**
     * Changes the slot where the item will be set.
     *
     * @param slot the new slot
     */
    public void setSlot(int slot) {
        this.slot = slot;
    }

    /**
     * Gets the item stack which will be set.
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Changes the item stack which will be set.
     *
     * @param itemStack the new item stack
     */
    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
