package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

/**
 * Called when {@link Player} unsuccessfully attempts to digging up a block;
 */
public class PlayerTryStartDiggingEvent implements PlayerInstanceEvent, BlockEvent {
    private final Player player;
    private final Block block;
    private final BlockVec blockPosition;
    private final BlockFace blockFace;

    public PlayerTryStartDiggingEvent(Player player, Block block, BlockVec blockPosition, BlockFace blockFace) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
    }

    /**
     * Gets the block which is being dug.
     *
     * @return the block
     */
    @Override
    public Block getBlock() {
        return block;
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

    /**
     * Gets the face you are digging
     *
     * @return the block face
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
