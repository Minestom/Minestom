package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import org.jspecify.annotations.Nullable;

public interface ProjectileMeta {

    @Nullable
    Entity getShooter();

    void setShooter(@Nullable Entity shooter);

}
