package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;

/**
 * Used when a player is clicking a block with an item (but is not a block in item form)
 */
public class PlayerUseItemOnBlockEvent extends Event {

    private final Player player;
    private final Player.Hand hand;
    private final ItemStack itemStack;
    private final BlockPosition position;
    private final Direction blockFace;

    public PlayerUseItemOnBlockEvent(Player player, Player.Hand hand, ItemStack itemStack, BlockPosition position, Direction blockFace) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
        this.position = position;
        this.blockFace = blockFace;
    }

    /**
     * Get the player who used an item while clicking on a block
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the position of the interacted block
     *
     * @return the block position
     */
    public BlockPosition getPosition() {
        return position;
    }

    /**
     * Get which face the player has interacted with
     *
     * @return the block face
     */
    public Direction getBlockFace() {
        return blockFace;
    }

    /**
     * Get which hand the player used to interact with the block
     *
     * @return the hand
     */
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Get with which item the player has interacted with the block
     *
     * @return the item
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
}
