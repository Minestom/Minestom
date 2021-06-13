package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.block.Block;
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

    private final Block block;
    private final BlockPosition blockPosition;

    private boolean cancelled;

    public PlayerStartDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull BlockPosition blockPosition) {
        super(player);
        this.block = block;
        this.blockPosition = blockPosition;
    }

    /**
     * Gets the block which is being dug.
     *
     * @return the block
     */
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the {@link BlockPosition}.
     *
     * @return the {@link BlockPosition}
     */
    public @NotNull BlockPosition getBlockPosition() {
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
}
