package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

/**
 * Called when a {@link Player} start digging a block,
 * can be used to forbid the {@link Player} from mining it.
 * <p>
 * Be aware that cancelling this event does not necessary prevent the player from breaking the block
 * (could be because of high latency or a modified client) so cancelling {@link PlayerBlockBreakEvent} is also necessary.
 * Could be fixed in future Minestom version.
 */
public class PlayerStartDiggingEvent implements PlayerEvent, BlockInstanceEvent, CancellableEvent {

    private final Player player;
    private final Instance instance;
    private final Block block;
    private final BlockVec blockPosition;
    private final BlockFace blockFace;

    private boolean cancelled;

    public PlayerStartDiggingEvent(Player player, Instance instance, Block block,
                                   BlockVec blockPosition,
                                   BlockFace blockFace) {
        this.player = player;
        this.instance = instance;
        this.block = block;
        this.blockPosition = blockPosition;
        this.blockFace = blockFace;
    }

    @Override
    public Instance getInstance() {
        return instance;
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
