package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

/**
 * Called when a player interacts with a block (right-click).
 * This is also called when a block is placed.
 */
public class PlayerBlockInteractEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final PlayerHand hand;
    private final Block block;
    private final BlockVec blockPosition;
    private final Point cursorPosition;
    private final BlockFace blockFace;

    /**
     * Does this interaction block the normal item use?
     * True for containers which open an inventory instead of letting blocks be placed
     */
    private boolean blocksItemUse;

    private boolean cancelled;

    public PlayerBlockInteractEvent(Player player, PlayerHand hand,
                                    Block block, BlockVec blockPosition, Point cursorPosition,
                                    BlockFace blockFace) {
        this.player = player;
        this.hand = hand;
        this.block = block;
        this.blockPosition = blockPosition;
        this.cursorPosition = cursorPosition;
        this.blockFace = blockFace;
    }

    /**
     * Gets if the event should block the item use.
     *
     * @return true if the item use is blocked, false otherwise
     */
    public boolean isBlockingItemUse() {
        return blocksItemUse;
    }

    /**
     * Sets the blocking item use state of this event
     * Note: If this is true, then no {@link PlayerUseItemOnBlockEvent} will be fired.
     * @param blocks - true to block item interactions, false to not block
     */
    public void setBlockingItemUse(boolean blocks) {
        this.blocksItemUse = blocks;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the position of the interacted block.
     *
     * @return the block position
     */
    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the cursor position of the interacted block
     * @return the cursor position of the interaction
     */
    public Point getCursorPosition() { return cursorPosition; }

    /**
     * Gets the hand used for the interaction.
     *
     * @return the hand used
     */
    public PlayerHand getHand() {
        return hand;
    }

    /**
     * Gets the block face.
     *
     * @return the block face
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
