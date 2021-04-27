package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

public class ItemUseOnBlockEvent extends PlayerUseItemOnBlockEvent {

    public ItemUseOnBlockEvent(@NotNull Player player, @NotNull Player.Hand hand,
                                     @NotNull ItemStack itemStack,
                                     @NotNull BlockPosition position, @NotNull Direction blockFace) {
        super(player, hand, itemStack, position, blockFace);
    }

}
