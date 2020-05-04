package net.minestom.server.event;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;

/**
 * Used when a player is clicking a block with an item (but is not a block in item form)
 */
public class PlayerUseItemOnBlockEvent extends Event {

    private Player.Hand hand;
    private ItemStack itemStack;
    private final BlockPosition position;
    private final Direction blockFace;

    public PlayerUseItemOnBlockEvent(Player.Hand hand, ItemStack itemStack, BlockPosition position, Direction blockFace) {
        this.hand = hand;
        this.itemStack = itemStack;
        this.position = position;
        this.blockFace = blockFace;
    }

    public BlockPosition getPosition() {
        return position;
    }

    public Direction getBlockFace() {
        return blockFace;
    }

    public Player.Hand getHand() {
        return hand;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
