package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;

/**
 * Used when a player is clicking on a block with an item (but is not a block in item form).
 */
public class PlayerUseItemOnBlockEvent implements PlayerInstanceEvent, ItemEvent, BlockEvent {

    private final Player player;
    private final PlayerHand hand;
    private final ItemStack itemStack;
    private final Block block;
    private final BlockVec blockPosition;
    private final BlockFace blockFace;
    private final Point cursorPosition;

    public PlayerUseItemOnBlockEvent(Player player, PlayerHand hand,
                                     ItemStack itemStack,
                                     Block block, BlockVec blockPosition, BlockFace blockFace,
                                     Point cursorPosition) {
        this.player = player;
        this.hand = hand;
        this.itemStack = itemStack;
        this.block = block;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
        this.cursorPosition = cursorPosition;
    }



    @Override
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Gets which hand the player used to interact with the block.
     *
     * @return the hand
     */
    public PlayerHand getHand() {
        return this.hand;
    }

    /**
     * Gets with which item the player has interacted with the block.
     *
     * @return the item
     */
    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Gets the interacted block.
     *
     * @return the block
     */
    @Override
    public Block getBlock() {
        return this.block;
    }

    /**
     * Gets the position of the interacted block.
     *
     * @return the block position
     */
    @Override
    public BlockVec getBlockPosition() {
        return this.blockPosition;
    }

    /**
     * Gets which face the player has interacted with.
     *
     * @return the block face
     */
    public BlockFace getBlockFace() {
        return this.blockFace;
    }

    /**
     * Gets the cursor position of the interacted block
     *
     * @return the cursor position of the interaction
     */
    public Point getCursorPosition() {
        return this.cursorPosition;
    }

    /**
     * Gets the position of the interacted block.
     *
     * @deprecated Use {@link #getBlockPosition()} instead.
     * @return the block position
     */
    @Deprecated
    public Point getPosition() { return this.blockPosition; }
}
