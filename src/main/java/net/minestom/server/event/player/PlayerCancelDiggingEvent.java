package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} stops digging a block before it is broken
 */
public class PlayerCancelDiggingEvent implements PlayerInstanceEvent, BlockEvent {
    private final Player player;
    private final Block block;
    private final Point blockPosition;

    public PlayerCancelDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull Point blockPosition) {
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
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    public @NotNull Point getBlockPosition() {
        return blockPosition;
    }
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
