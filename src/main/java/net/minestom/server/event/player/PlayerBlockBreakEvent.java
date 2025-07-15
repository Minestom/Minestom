package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

public class PlayerBlockBreakEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final Block block;
    private Block resultBlock;
    private final BlockVec blockPosition;
    private final BlockFace blockFace;

    private boolean cancelled;

    public PlayerBlockBreakEvent(Player player,
                                 Block block, Block resultBlock, BlockVec blockPosition,
                                 BlockFace blockFace) {
        this.player = player;

        this.block = block;
        this.resultBlock = resultBlock;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
    }

    /**
     * Gets the block to break
     *
     * @return the block
     */
    @Override
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the block which will replace {@link #getBlock()}.
     *
     * @return the result block
     */
    public Block getResultBlock() {
        return resultBlock;
    }

    /**
     * Gets the face at which the block was broken
     *
     * @return the block face
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Changes the result of the event.
     *
     * @param resultBlock the new block
     */
    public void setResultBlock(Block resultBlock) {
        this.resultBlock = resultBlock;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
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
