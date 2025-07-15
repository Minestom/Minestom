package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;

/**
 * Used when a player is clicking on a block with an item (but is not a block in item form).
 */
public class PlayerUseItemOnBlockEvent implements PlayerInstanceEvent, ItemEvent {

    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;
    private final Point position;
    private final BlockFace blockFace;

    public PlayerUseItemOnBlockEvent(Player player, PlayerHand hand,
                                     ItemStack itemStack,
                                     Point position, Point cursorPosition,
                                     BlockFace blockFace) {
        this.player = player;
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
    public Point getPosition() {
        return position;
    }

    /**
     * Gets which face the player has interacted with.
     *
     * @return the block face
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Gets which hand the player used to interact with the block.
     *
     * @return the hand
     */
    public PlayerHand getHand() {
        return hand;
    }

    /**
     * Gets with which item the player has interacted with the block.
     *
     * @return the item
     */
    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
