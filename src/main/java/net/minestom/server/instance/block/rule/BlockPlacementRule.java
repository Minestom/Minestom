package net.minestom.server.instance.block.rule;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public abstract class BlockPlacementRule {

    private final Block block;

    public BlockPlacementRule(@NotNull Block block) {
        this.block = block.getDefaultBlock();
    }

    /**
     * Called when the block state id can be updated (for instance if a neighbour block changed).
     *
     * @param instance      the instance of the block
     * @param blockPosition the block position
     * @param block         the current block
     * @return the updated block
     */
    public abstract Block blockUpdate(@NotNull Instance instance, @NotNull BlockPosition blockPosition, Block block);

    /**
     * Called when the block is placed.
     *
     * @param instance      the instance of the block
     * @param block         the block placed
     * @param blockFace     the block face
     * @param blockPosition the block position
     * @param pl            the player who placed the block
     * @return the block to place, null to prevent the placement
     */
    public abstract Block blockPlace(@NotNull Instance instance,
                                     @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                                     @NotNull Player pl);

    public Block getBlock() {
        return block;
    }
}
