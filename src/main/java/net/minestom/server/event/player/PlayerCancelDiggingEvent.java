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
public class PlayerCancelDiggingEvent implements PlayerInstanceEvent, BlockEvent {
    private final Player player;
    private final BlockEvent.Source.Player source;
    private final Block block;
    private final BlockVec blockPosition;

    public PlayerCancelDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull BlockVec blockPosition, @NotNull BlockEvent.Source.Player source) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
        this.source = source;
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
    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the {@link BlockEvent.Source}
     *
     * @return the Events Source
     */
    @Override
    public @NotNull BlockEvent.Source.Player getSource() {
        return source;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
