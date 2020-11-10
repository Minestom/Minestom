package net.minestom.server.utils.location;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

public class RelativeBlockPosition extends RelativeLocation<BlockPosition> {

    public RelativeBlockPosition(BlockPosition location, boolean relativeX, boolean relativeY, boolean relativeZ) {
        super(location, relativeX, relativeY, relativeZ);
    }

    @Override
    public BlockPosition fromRelativePosition(Entity entity) {
        if (!relativeX && !relativeY && !relativeZ) {
            return location.copy();
        }
        final Position entityPosition = entity.getPosition();

        final int x = relativeX ? (int) entityPosition.getX() : location.getX();
        final int y = relativeY ? (int) entityPosition.getY() : location.getY();
        final int z = relativeZ ? (int) entityPosition.getZ() : location.getZ();

        return new BlockPosition(x, y, z);
    }
}
