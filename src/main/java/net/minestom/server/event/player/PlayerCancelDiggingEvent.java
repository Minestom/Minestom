package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} stops digging a block before it is broken
 */
public record PlayerCancelDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull BlockVec blockPosition) implements PlayerInstanceEvent, BlockEvent {
    /**
     * Gets the block which was being dug.
     *
     * @return the block
     */
    @Override
    public @NotNull Block block() {
        return block;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public @NotNull BlockVec blockPosition() {
        return blockPosition;
    }

    @Override
    public @NotNull Player player() {
        return player;
    }
}
