package net.minestom.server.instance.block.rule;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import org.jetbrains.annotations.NotNull;

public abstract class BlockPlacementRule {
    public static final int DEFAULT_UPDATE_RANGE = 10;

    protected final Block block;

    protected BlockPlacementRule(@NotNull Block block) {
        this.block = block;
    }

    /**
     * Called when the block state id can be updated (for instance if a neighbour block changed).
     * This is first called on a newly placed block, and then this is called for all neighbors of the block
     */
    public @NotNull Block blockUpdate(@NotNull BlockChange blockChange) {
        return blockChange.block();
    }

    /**
     * Called when the block is placed.
     * It is recommended that you only set up basic properties on the block for this placement, such as determining facing, etc
     *
     * @return the block to place, {@code null} to cancel
     */
    public abstract @NotNull Block blockPlace(@NotNull BlockChange blockChange);

    public boolean isSelfReplaceable(@NotNull BlockChange.Replacement blockChange) {
        return false;
    }

    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * The max distance where a block update can be triggered. It is not based on block, so if the value is 3 and a completely
     * different block updates 3 blocks away it could still trigger an update.
     */
    public int maxUpdateDistance() {
        return DEFAULT_UPDATE_RANGE;
    }
}
