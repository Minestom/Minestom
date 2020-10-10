package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.BlockPosition;

/**
 * Called when a player start digging a block,
 * can be used to forbid the player from mining it.
 */
public class PlayerStartDiggingEvent extends CancellableEvent {

    private final Player player;
    private final BlockPosition blockPosition;
    private final int blockStateId;
    private final int customBlockId;

    public PlayerStartDiggingEvent(Player player, BlockPosition blockPosition, int blockStateId, int customBlockId) {
        this.player = player;
        this.blockPosition = blockPosition;
        this.blockStateId = blockStateId;
        this.customBlockId = customBlockId;
    }

    /**
     * Get the player who started digging the block
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the block position
     *
     * @return the block position
     */
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * Get the block state id
     *
     * @return the block state id
     */
    public int getBlockStateId() {
        return blockStateId;
    }

    /**
     * Get the custom block id
     *
     * @return the custom block id
     */
    public int getCustomBlockId() {
        return customBlockId;
    }
}
