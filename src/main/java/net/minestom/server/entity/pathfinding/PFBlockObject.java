package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.linalg.immutable.AxisAlignedBBox;
import net.minestom.server.instance.block.Block;

public class PFBlockObject implements IBlockObject {

    private Block block;

    public PFBlockObject(Block block) {
        this.block = block;
    }

    @Override
    public AxisAlignedBBox bounds() {
        return new AxisAlignedBBox(
                0, 0, 0,
                1, 1, 1
        );
    }

    @Override
    public boolean isFenceLike() {
        return block.name().toLowerCase().contains("FENCE");
    }

    @Override
    public boolean isClimbable() {
        return false;
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
        return false;
    }
}
