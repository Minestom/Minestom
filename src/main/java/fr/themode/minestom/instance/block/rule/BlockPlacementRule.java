package fr.themode.minestom.instance.block.rule;

import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.utils.BlockPosition;

public abstract class BlockPlacementRule {

    private short blockId;

    public BlockPlacementRule(short blockId) {
        this.blockId = blockId;
    }

    public BlockPlacementRule(Block block) {
        this(block.getBlockId());
    }

    public abstract boolean canPlace(Instance instance, BlockPosition blockPosition);

    public abstract short blockRefresh(Instance instance, BlockPosition blockPosition);

    public short getBlockId() {
        return blockId;
    }
}
