package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.MetaTarget;
import org.jetbrains.annotations.Nullable;

public interface ProjectileMeta {

    @Nullable
    MetaTarget getShooter();

    void setShooter(@Nullable MetaTarget shooter);

}
