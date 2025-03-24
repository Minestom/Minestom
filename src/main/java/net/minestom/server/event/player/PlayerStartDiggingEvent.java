package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} start digging a block,
 * can be used to forbid the {@link Player} from mining it.
 * <p>
 * Be aware that cancelling this event does not necessary prevent the player from breaking the block
 * (could be because of high latency or a modified client) so cancelling {@link net.minestom.server.event.instance.InstanceBlockChangeEvent} is also necessary.
 * Could be fixed in future Minestom version.
 */
public class PlayerStartDiggingEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {

    private final Source.Player source;
    private final Block block;
    private final BlockVec blockPosition;

    private boolean cancelled;

    public PlayerStartDiggingEvent(@NotNull Source.Player source, @NotNull Block block, @NotNull BlockVec blockPosition) {
        this.source = source;
        this.block = block;
        this.blockPosition = blockPosition;
    }

    /**
     * Gets the block which is being dug.
     *
     * @return the block
     */
    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the face you are digging
     *
     * @return the block face
     */
    public @NotNull BlockFace getBlockFace() {
        return source.blockFace();
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
    public @NotNull Player getPlayer() {
        return source.player();
    }

    @Override
    public @NotNull Source.Player getSource() {
        return source;
    }
}
