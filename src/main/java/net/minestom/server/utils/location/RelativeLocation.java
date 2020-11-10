package net.minestom.server.utils.location;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RelativeLocation<T> {

    protected T location;
    protected boolean relativeX, relativeY, relativeZ;

    public RelativeLocation(@NotNull T location, boolean relativeX, boolean relativeY, boolean relativeZ) {
        this.location = location;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.relativeZ = relativeZ;
    }

    public abstract T fromRelativePosition(@Nullable Entity entity);

}
