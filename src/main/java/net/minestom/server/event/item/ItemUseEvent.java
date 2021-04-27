package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Event when an item is used without clicking on a block.
 */
public class ItemUseEvent extends PlayerUseItemEvent {

    public ItemUseEvent(@NotNull Player player, @NotNull Player.Hand hand, @NotNull ItemStack itemStack) {
        super(player, hand, itemStack);
    }
}
