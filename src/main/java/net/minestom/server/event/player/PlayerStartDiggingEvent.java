package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} start digging a block,
 * can be used to forbid the {@link Player} from mining it.
 */
public class PlayerStartDiggingEvent extends CancellableEvent {

    private final Player player;
    private final BlockPosition blockPosition;
    private final int blockStateId;
    private final int customBlockId;

    public PlayerStartDiggingEvent(@NotNull Player player, @NotNull BlockPosition blockPosition, int blockStateId, int customBlockId) {
        this.player = player;
        this.blockPosition = blockPosition;
        this.blockStateId = blockStateId;
        this.customBlockId = customBlockId;
    }

    /**
     * Gets the {@link Player} who started digging the block.
     *
     * @return the {@link Player}
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link BlockPosition}.
     *
     * @return the {@link BlockPosition}
     */
    @NotNull
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the block state id.
     *
     * @return the block state id
     */
    public int getBlockStateId() {
        return blockStateId;
    }

    /**
     * Gets the custom block id.
     *
     * @return the custom block id
     */
    public int getCustomBlockId() {
        return customBlockId;
    }
}
