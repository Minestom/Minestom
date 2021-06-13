package net.minestom.server.block.rule;

import net.minestom.server.block.BlockFace;
import net.minestom.server.entity.Player;
import net.minestom.server.world.World;
import net.minestom.server.block.Block;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockPlacementRule {

    public static final int CANCEL_CODE = -1;

    private final Block block;

    public BlockPlacementRule(@NotNull Block block) {
        this.block = block;
    }

    /**
     * Called when the block state id can be updated (for example if a neighbour block changed).
     *
     * @param world         the World of the block
     * @param blockPosition the block position
     * @param currentBlock  the current block
     * @return the updated block
     */
    public abstract @NotNull Block blockUpdate(@NotNull World world, @NotNull BlockPosition blockPosition, @NotNull Block currentBlock);

    /**
     * Called when the block is placed.
     *
     * @param world         the World of the block
     * @param block         the block placed
     * @param blockFace     the block face
     * @param blockPosition the block position
     * @param pl            the player who placed the block
     * @return the block to place, {@code null} to cancel
     */
    public abstract @Nullable Block blockPlace(@NotNull World world,
                                               @NotNull Block block, @NotNull BlockFace blockFace, @NotNull BlockPosition blockPosition,
                                               @NotNull Player pl);

    public @NotNull Block getBlock() {
        return block;
    }
}
