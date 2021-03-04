package net.minestom.server.utils.location;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a relative {@link BlockPosition}.
 *
 * @see RelativeLocation
 */
public class RelativeBlockPosition extends RelativeLocation<BlockPosition> {

    public RelativeBlockPosition(BlockPosition location, boolean relativeX, boolean relativeY, boolean relativeZ) {
        super(location, relativeX, relativeY, relativeZ);
    }

    @Override
    public BlockPosition from(@Nullable Position position) {
        if (!relativeX && !relativeY && !relativeZ) {
            return location.clone();
        }
        final Position entityPosition = position != null ? position : new Position();

        final int x = location.getX() + (relativeX ? (int) entityPosition.getX() : 0);
        final int y = location.getY() + (relativeY ? (int) entityPosition.getY() : 0);
        final int z = location.getZ() + (relativeZ ? (int) entityPosition.getZ() : 0);

        return new BlockPosition(x, y, z);
    }
}
