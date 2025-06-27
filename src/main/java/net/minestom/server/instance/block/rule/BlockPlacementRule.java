package net.minestom.server.instance.block.rule;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
<<<<<<< HEAD
import net.minestom.server.item.ItemStack;
=======
import net.minestom.server.instance.block.BlockChange;
>>>>>>> cc02c79fb (Cleanup)
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockPlacementRule {
    public static final int DEFAULT_UPDATE_RANGE = 10;

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
<<<<<<< HEAD
    public @NotNull Block blockUpdate(@NotNull UpdateState updateState) {
        return updateState.currentBlock();
=======
    public @NotNull Block blockUpdate(@NotNull BlockChange mutation) {
        return mutation.block();
>>>>>>> cc02c79fb (Cleanup)
    }

    /**
     * Called when the block is placed.
     * It is recommended that you only set up basic properties on the block for this placement, such as determining facing, etc
     *
     * @param placementState The current parameters to the block placement
     * @return the block to place, {@code null} to cancel
     */
<<<<<<< HEAD
    public abstract @Nullable Block blockPlace(@NotNull PlacementState placementState);
=======
    public abstract @NotNull Block blockPlace(@NotNull BlockChange mutation);
>>>>>>> cc02c79fb (Cleanup)

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
                              @NotNull Point blockPosition,
                              @NotNull Block currentBlock,
                              @NotNull BlockFace fromFace) {
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
