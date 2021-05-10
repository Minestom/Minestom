package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} start digging a block,
 * can be used to forbid the {@link Player} from mining it.
 * <p>
 * Be aware that cancelling this event does not necessary prevent the player from breaking the block
 * (could be because of high latency or a modified client) so cancelling {@link PlayerBlockBreakEvent} is also necessary.
 * Could be fixed in future Minestom version.
 */
public class PlayerStartDiggingEvent extends PlayerEvent implements CancellableEvent {

    private final BlockPosition blockPosition;
    private final int blockStateId;
    private final int customBlockId;

    private boolean cancelled;

    public PlayerStartDiggingEvent(@NotNull Player player, @NotNull BlockPosition blockPosition, int blockStateId, int customBlockId) {
        super(player);
        this.blockPosition = blockPosition;
        this.blockStateId = blockStateId;
        this.customBlockId = customBlockId;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
