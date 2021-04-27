package net.minestom.server.event.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} holding an {@link net.minestom.server.item.ItemStack} interacts (right-click) with an {@link Entity}.
 */
public class ItemEntityInteractEvent extends PlayerEntityInteractEvent {

    private final ItemStack itemStack;

    public ItemEntityInteractEvent(@NotNull ItemStack itemStack, @NotNull Player player, @NotNull Entity entityTarget, @NotNull Player.Hand hand) {
        super(player, entityTarget, hand);
        this.itemStack = itemStack;
    }

    /**
     * Gets the item stack that was used to interact with the entity
     *
     * @return the item stack
     */
    public @NotNull ItemStack getItemStack() {
        return this.itemStack;
    }
}