package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called as a result of {@link net.minestom.server.inventory.PlayerInventory#addItemStack(ItemStack)}.
 */
public class PlayerAddItemStackEvent extends CancellableEvent {

    private final Player player;
    private ItemStack itemStack;

    public PlayerAddItemStackEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    /**
     * Gets the player who has an item stack added to his inventory.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the item stack which will be added.
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Changes the item stack which will be added.
     *
     * @param itemStack the new item stack
     */
    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack =itemStack;
    }
}
