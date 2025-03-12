package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} successfully finishes digging a block
 */
public class PlayerFinishDiggingEvent implements PlayerInstanceEvent, BlockEvent {
    private @NotNull Block block;
    private final BlockVec blockPosition;
    private final BlockEvent.Source.Player source;

    public PlayerFinishDiggingEvent( @NotNull Block block, @NotNull BlockVec blockPosition, @NotNull BlockEvent.Source.Player source) {
        this.block = block;
        this.blockPosition = blockPosition;
        this.source = source;
    }

    /**
     * Changes which block was dug
     * <p>
     * This has somewhat odd behavior;
     * If you set it from a previously solid block to a non-solid block
     * then cancel the respective {@link net.minestom.server.event.block.BlockChangeEvent}
     * it will allow the player to phase through the block and into the floor
     * (only if the player is standing on top of the block)
     *
     * @param block the block to set the result to
     */
    public void setBlock(@NotNull Block block) {
        this.block = block;
    }

    /**
     * Gets the block which was dug.
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
    public @NotNull BlockEvent.Source getSource() {
        return source;
    }

    @Override
    public @NotNull Player getPlayer() {
        return source.player();
    }
}
