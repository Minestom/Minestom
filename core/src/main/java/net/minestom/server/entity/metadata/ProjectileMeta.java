package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface ProjectileMeta {

    @Nullable
    Entity getShooter();

    void setShooter(@Nullable Entity shooter);

}
