package net.minestom.server.event.player;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries placing a block.
 */
public class PlayerBlockUpdateNeighborEvent implements PlayerEvent, EntityInstanceEvent, BlockEvent {

    private final Player player;
    private Block block;
    private final Point blockPosition;
    private boolean shouldUpdateNeighbors = false;

    public PlayerBlockUpdateNeighborEvent(@NotNull Player player, @NotNull Block block,
                                          @NotNull Point blockPosition) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
    }

    /**
     * Gets the block which will be placed.
     *
     * @return the block to place
     */
    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Changes the block to be placed.
     *
     * @param block the new block
     */
    public void setBlock(@NotNull Block block) {
        this.block = block;
    }


    /**
     * Gets the block position.
     *
     * @return the block position
     */
    public @NotNull Point getBlockPosition() {
        return blockPosition;
    }

    public boolean isShouldUpdateNeighbors() {
        return shouldUpdateNeighbors;
    }

    public void setShouldUpdateNeighbors(boolean shouldUpdateNeighbors) {
        this.shouldUpdateNeighbors = shouldUpdateNeighbors;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
