package fr.themode.minestom.instance.block.rule;

import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.utils.BlockPosition;

public abstract class BlockPlacementRule {

    private Block block;

    public BlockPlacementRule(Block block) {
        this.block = block;
    }

    public abstract short blockRefresh(Instance instance, BlockPosition blockPosition);

    public Block getBlock() {
        return block;
    }
}
