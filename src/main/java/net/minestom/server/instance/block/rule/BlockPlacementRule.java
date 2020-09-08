package net.minestom.server.instance.block.rule;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.utils.BlockPosition;

public abstract class BlockPlacementRule {

    private short blockId;

    public BlockPlacementRule(short blockId) {
        this.blockId = blockId;
    }

    public BlockPlacementRule(Block block) {
        this(block.getBlockId());
    }

    /**
     * Get if the block can be placed in {@code blockPosition}
     * Can for example, be used for blocks which have to be placed on a solid block
     *
     * @param instance      the instance of the block
     * @param blockPosition the position where the block is trying to get place
     * @return true if the block placement position is valid
     */
    public abstract boolean canPlace(Instance instance, BlockPosition blockPosition);

    /**
     * Called when the block state id can be updated (for instance if a neighbour block changed)
     *
     * @param instance       the instance of the block
     * @param blockPosition  the block position
     * @param currentStateID the current block state id of the block
     * @return the updated block state id
     */
    public abstract short blockRefresh(Instance instance, BlockPosition blockPosition, short currentStateID);

    /**
     * Called when the block is placed
     *
     * @param instance  the instance of the block
     * @param block     the block placed
     * @param blockFace the block face
     * @param pl        the player who placed the block
     * @return the block state id of the placed block
     */
    public abstract short blockPlace(Instance instance, Block block, BlockFace blockFace, Player pl);

    public short getBlockId() {
        return blockId;
    }
}
