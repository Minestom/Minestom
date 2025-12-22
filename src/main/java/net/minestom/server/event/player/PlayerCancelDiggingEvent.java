package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

/**
 * Called when a {@link Player} stops digging a block before it is broken
 */
public class PlayerCancelDiggingEvent implements PlayerEvent, BlockInstanceEvent {
    private final Player player;
    private final Instance instance;
    private final Block block;
    private final BlockVec blockPosition;

    public PlayerCancelDiggingEvent(Player player, Instance instance, Block block, BlockVec blockPosition) {
        this.player = player;
        this.instance = instance;
        this.block = block;
        this.blockPosition = blockPosition;
    }

    @Override
    public Instance getInstance() {
        return instance;
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
