package net.minestom.server.instance.block.rule;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockPlacementRule {
    private final Block block;

    public BlockPlacementRule(@NotNull Block block) {
        this.block = block;
    }

    /**
     * Called when the block state id can be updated (for instance if a neighbour block changed).
     *
     * @param instance      the instance of the block
     * @param blockPosition the block position
     * @param currentBlock  the current block
     * @return the updated block
     */
    public abstract @NotNull Block blockUpdate(@NotNull Instance instance, @NotNull Point blockPosition, @NotNull Block currentBlock);

    /**
     * Called when the block is placed.
     *
     * @param instance      the instance of the block
     * @param block         the block placed
     * @param blockFace     the block face
     * @param blockPosition the block position
     * @param pl            the player who placed the block
     * @return the block to place, {@code null} to cancel
     * @deprecated use {@link #blockPlace(Instance, Block, BlockFace, Point, Player, Point)} instead
     */
    @Deprecated
    public @Nullable Block blockPlace(@NotNull Instance instance,
                                      @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Point blockPosition,
                                      @NotNull Player pl) {
        return block;
    }

    /**
     * Called when the block is placed.
     *
     * @param instance       the instance of the block
     * @param block          the block placed
     * @param blockFace      the block face
     * @param blockPosition  the block position
     * @param pl             the player who placed the block
     * @param cursorPosition the position of the cursor on the clicked block
     * @return the block to place, {@code null} to cancel
     */
    public @Nullable Block blockPlace(@NotNull Instance instance,
                                      @NotNull Block block, @NotNull BlockFace blockFace, @NotNull Point blockPosition,
                                      @NotNull Player pl, @NotNull Point cursorPosition) {
        return blockPlace(instance, block, blockFace, blockPosition, pl);
    }

    public @NotNull Block getBlock() {
        return block;
    }
}
