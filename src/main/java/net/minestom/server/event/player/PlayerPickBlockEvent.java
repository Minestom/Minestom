package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries to pick a block (middle-click).
 */
public class PlayerPickBlockEvent implements PlayerInstanceEvent, BlockEvent {

    private final Player player;

    private final Block block;
    private final BlockVec blockPosition;
    private final boolean includeData;

    public PlayerPickBlockEvent(@NotNull Player player, @NotNull Block block,
                                @NotNull BlockVec blockPosition, boolean includeData) {
        this.player = player;

        this.block = block;
        this.blockPosition = blockPosition;
        this.includeData = includeData;
    }

    /**
     * Gets the block which was picked.
     *
     * @return the block which was picked
     */
    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Gets the picked block position.
     *
     * @return the picked block position
     */
    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Get if the entity data should be included in the result (control middle-click).
     *
     * @return if the entity data should be included.
     */
    public boolean isIncludeData() {
        return this.includeData;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }
}
