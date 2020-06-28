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

    public abstract boolean canPlace(Instance instance, BlockPosition blockPosition);

    public abstract short blockRefresh(Instance instance, BlockPosition blockPosition, short currentID);

    public abstract short blockPlace(Instance instance, Block block, BlockFace blockFace, Player pl);

    public short getBlockId() {
        return blockId;
    }
}
