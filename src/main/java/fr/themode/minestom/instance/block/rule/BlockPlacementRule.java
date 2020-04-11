package fr.themode.minestom.instance.block.rule;

import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.Block;
import fr.themode.minestom.utils.BlockPosition;

public abstract class BlockPlacementRule {

    private Block block;

    public BlockPlacementRule(Block block) {
        this.block = block;
    }

    public abstract void onPlace(Instance instance, BlockPosition blockPosition);

    public abstract void onNeighborPlace(Instance instance, int offsetX, int offsetY, int offsetZ);

    public Block getBlock() {
        return block;
    }
}
