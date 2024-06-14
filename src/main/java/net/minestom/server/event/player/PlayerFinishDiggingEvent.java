package net.minestom.server.event.player;

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
    private final Player player;
    private @NotNull Block block;
    private final Point blockPosition;

    public PlayerFinishDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull Point blockPosition) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
    }

    /**
     * Changes which block was dug
     * <p>
     * This has somewhat odd behavior;
     * If you set it from a previously solid block to a non-solid block
     * then cancel the respective {@link PlayerBlockBreakEvent}
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
    public @NotNull Point getBlockPosition() {
        return blockPosition;
    }
    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
