package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;

/**
 * Called when a {@link Player} stops digging a block before it is broken
 */
public class PlayerCancelDiggingEvent implements PlayerInstanceEvent, BlockEvent {
    private final Player player;
    private final Block block;
    private final BlockVec blockPosition;

    public PlayerCancelDiggingEvent(Player player, Block block, BlockVec blockPosition) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
    }

    /**
     * Gets the block which was being dug.
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

    @Override
    public Player getPlayer() {
        return player;
    }
}
