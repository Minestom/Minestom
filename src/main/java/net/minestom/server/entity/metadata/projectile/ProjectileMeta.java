package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface ProjectileMeta {

    /**
     * The projectile was shot from this entity
     * <p>
     * Stored as a weak reference.
     *
     * @return the entity
     */
    @Nullable Entity getShooter();

    /**
     * Set the shooter for #{@link #getShooter()}
     * <p>
     * Stored as a weak reference.
     *
     * @param shooter the shooter
     */
    void setShooter(@Nullable Entity shooter);

}
