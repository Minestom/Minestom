package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockDescription;
import net.minestom.server.instance.block.Block;

public class PFBlockDescription implements IBlockDescription {

    private Block block;

    public PFBlockDescription(Block block) {
        this.block = block;
    }

    @Override
    public boolean isFenceLike() {
        return block.name().toLowerCase().contains("FENCE");
    }

    @Override
    public boolean isClimbable() {
        return block == Block.LADDER;
    }

    @Override
    public boolean isDoor() {
        return block.name().toLowerCase().contains("DOOR");
    }

    @Override
    public boolean isImpeding() {
        return block.isSolid();
    }

    @Override
    public boolean isFullyBounded() {
        return block.isSolid();
    }

    @Override
    public boolean isLiquid() {
        return block.isLiquid();
    }

    @Override
    public boolean isIncinerating() {
        return block == Block.LAVA || block == Block.FIRE;
    }
}
