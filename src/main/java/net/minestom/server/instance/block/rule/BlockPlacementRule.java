package net.minestom.server.instance.block.rule;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockPlacementRule {
    public static final int DEFAULT_UPDATE_RANGE = 10;

    public static final Vec[] BLOCK_UPDATE_SHAPE = {
        Direction.UP.vec(),
        Direction.DOWN.vec(),
        Direction.NORTH.vec(),
        Direction.SOUTH.vec(),
        Direction.EAST.vec(),
        Direction.WEST.vec()
    };

    protected final Block block;

    protected BlockPlacementRule(@NotNull Block block) {
        this.block = block;
    }

    /**
     * Called when the block state id can be updated (for instance if a neighbour block changed).
     * This is first called on a newly placed block, and then this is called for all neighbors of the block
     *
     * @param updateState The current parameters to the block update
     * @return the updated block
     */
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        return updateState.currentBlock();
    }

    /**
     * Called when the block is placed.
     * It is recommended that you only set up basic properties on the block for this placement, such as determining facing, etc
     *
     * @param placementState The current parameters to the block placement
     * @return the block to place, {@code null} to cancel
     */
    public abstract @Nullable Block blockPlace(@NotNull PlacementState placementState);

    /**
     * Called to determine if the block should be updated based on the offset and the block that is being placed.
     * This is used to determine if the block should be updated when a new block signals this block to update.
     *
     * @param offset The offset from the current block position
     * @param block  The block that is being placed
     * @return {@code true} if the block will consider the update, {@code false} otherwise
     */
    public boolean considerUpdate(@NotNull Vec offset, @NotNull Block block) {
        // Check if the offset is one of the 6 cardinal directions by default using
        return offset.equals(Direction.UP.vec()) ||
                offset.equals(Direction.DOWN.vec()) ||
                offset.equals(Direction.NORTH.vec()) ||
                offset.equals(Direction.SOUTH.vec()) ||
                offset.equals(Direction.EAST.vec()) ||
                offset.equals(Direction.WEST.vec());
    }

    /**
     * The update shape of the block, used to determine which blocks should attempt to be updated by this block.
     *
     * @return the shape of the block
     */
    public @NotNull Vec[] updateShape() {
        return BLOCK_UPDATE_SHAPE;
    }

    public boolean isSelfReplaceable(@NotNull Replacement replacement) {
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

    public record PlacementState(
            @NotNull Block.Getter instance,
            @NotNull Block block,
            @Nullable BlockFace blockFace,
            @NotNull Point placePosition,
            @Nullable Point cursorPosition,
            @Nullable Pos playerPosition,
            @Nullable ItemStack usedItemStack,
            boolean isPlayerShifting
    ) {
    }

    public record UpdateState(@NotNull Block.Getter instance,
                              @NotNull Block currentBlock,
                              @NotNull Point blockPosition,
                              @NotNull Vec offset) {
    }

    public record Replacement(
            @NotNull Block block,
            @NotNull BlockFace blockFace,
            @NotNull Point cursorPosition,
            /**
			 * Whether or not the placement position is offset from the clicked block
			 * position.
			 */
            @NotNull boolean isOffset,
            @NotNull Material material
    ) {
    }
}
