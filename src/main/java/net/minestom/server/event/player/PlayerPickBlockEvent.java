package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;

/**
 * Called when a player tries to pick a block (middle-click).
 */
public class PlayerPickBlockEvent implements PlayerInstanceEvent, BlockEvent {

    private final Player player;

    private final Block block;
    private final BlockVec blockPosition;
    private final boolean includeData;

    public PlayerPickBlockEvent(Player player, Block block,
                                BlockVec blockPosition, boolean includeData) {
        this.player = player;

        this.block = block;
        this.blockPosition = blockPosition;
        this.includeData = includeData;
    }

    /**
     * Gets the block which was picked.
     *
     * @return the block which was picked
     */
    @Override
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the picked block position.
     *
     * @return the picked block position
     */
    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Get if the entity data should be included in the result (control middle-click).
     *
     * @return if the entity data should be included.
     */
    public boolean isIncludeData() {
        return this.includeData;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
