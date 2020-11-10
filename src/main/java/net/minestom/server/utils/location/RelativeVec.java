package net.minestom.server.utils.location;

import net.minestom.server.entity.Entity;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.Nullable;

public class RelativeVec extends RelativeLocation<Vector> {

    public RelativeVec(Vector location, boolean relativeX, boolean relativeY, boolean relativeZ) {
        super(location, relativeX, relativeY, relativeZ);
    }

    @Override
    public Vector fromRelativePosition(@Nullable Entity entity) {
        if (!relativeX && !relativeY && !relativeZ) {
            return location.copy();
        }
        final Position entityPosition = entity.getPosition();

        final float x = relativeX ? (int) entityPosition.getX() : location.getX();
        final float y = relativeY ? (int) entityPosition.getY() : location.getY();
        final float z = relativeZ ? (int) entityPosition.getZ() : location.getZ();

        return new Vector(x, y, z);
    }
}
