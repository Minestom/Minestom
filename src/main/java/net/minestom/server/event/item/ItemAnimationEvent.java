package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemAnimationEvent extends PlayerItemAnimationEvent {

    private final ItemStack itemStack;

    public ItemAnimationEvent(@NotNull ItemStack itemStack, @NotNull Player player, @NotNull ItemAnimationType armAnimationType) {
        super(player, armAnimationType);

        this.itemStack = itemStack;
    }

    /**
     * Gets the item stack that was animated
     *
     * @return the item stack
     */
    public @NotNull ItemStack getItemStack() {
        return this.itemStack;
    }

}
