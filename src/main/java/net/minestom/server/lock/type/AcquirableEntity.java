package net.minestom.server.lock.type;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class AcquirableEntity extends AcquirableImpl<Entity> {
    public AcquirableEntity(@NotNull Entity value) {
        super(value);
    }
}
