package net.minestom.server.instance.block.rule;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockChange;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public abstract class BlockPlacementRule {

    /**
     * The default update shape for blocks, which includes all 6 cardinal directions.
     * This is used when a block is placed and needs to determine which neighbors to update.
     * This is the same as the default block update shape &amp; order in Minecraft.
     */
    public static final @Unmodifiable @NotNull List<Vec> DEFAULT_BLOCK_UPDATE_SHAPE = List.of(
            Direction.WEST.vec(),
            Direction.EAST.vec(),
            Direction.NORTH.vec(),
            Direction.SOUTH.vec(),
            Direction.UP.vec(),
            Direction.DOWN.vec()
    );

    protected final Block block;

    protected BlockPlacementRule(Block block) {
        this.block = block;
    }

    /**
     * Called when the block state id can be updated (for instance if a neighbour block changed).
     * This is first called on a newly placed block, and then this is called for all neighbors of the block
     */
    public Block blockUpdate(BlockChange blockChange) {
        return blockChange.block();
    }

    /**
     * Called when the block is placed.
     * It is recommended that you only set up basic properties on the block for this placement, such as determining facing, etc
     *
     * @return the block to place, {@code null} to cancel
     */
    public abstract Block blockPlace(BlockChange blockChange);

    /**
     * Called to determine if the block should be updated based on the offset and the block that is being placed.
     * This is used to determine if the block should be updated when a new block signals this block to update.
     *
     * @param offset The offset from the current block position
     * @param block  The block that is being placed
     * @return {@code true} if the block will consider the update, {@code false} otherwise
     */
    public boolean considerUpdate(Vec offset, Block block) {
        for (Vec off : updateShape()) {
            if (off.samePoint(offset)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The update shape of the block, used to determine which blocks should attempt to be updated by this block.
     *
     * @return the shape of the block
     */
    public @Unmodifiable List<Vec> updateShape() {
        return DEFAULT_BLOCK_UPDATE_SHAPE;
    }

    public boolean isSelfReplaceable(BlockChange.Replacement blockChange) {
        return false;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isClientPredicted() {
        return false;
    }
}