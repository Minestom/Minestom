package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;

/**
 * Used when a player is clicking on a block with an item (but is not a block in item form).
 */
public class PlayerUseItemOnBlockEvent extends PlayerEvent {

    private final Player.Hand hand;
    private final ItemStack itemStack;
    private final BlockPosition position;
    private final Direction blockFace;

    public PlayerUseItemOnBlockEvent(@NotNull Player player, @NotNull Player.Hand hand,
                                     @NotNull ItemStack itemStack,
                                     @NotNull BlockPosition position, @NotNull Direction blockFace) {
        super(player);
        this.hand = hand;
        this.itemStack = itemStack;
        this.position = position;
        this.blockFace = blockFace;
    }

    /**
     * Gets the position of the interacted block.
     *
     * @return the block position
     */
    @NotNull
    public BlockPosition getPosition() {
        return position;
    }

    /**
     * Gets which face the player has interacted with.
     *
     * @return the block face
     */
    @NotNull
    public Direction getBlockFace() {
        return blockFace;
    }

    /**
     * Gets which hand the player used to interact with the block.
     *
     * @return the hand
     */
    @NotNull
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Gets with which item the player has interacted with the block.
     *
     * @return the item
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }
}
