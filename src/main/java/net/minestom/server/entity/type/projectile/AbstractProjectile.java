package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.type.Projectile;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractProjectile extends Entity implements Projectile {

    private final Entity shooter;

    public AbstractProjectile(@Nullable Entity shooter, @NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setGravity(0.02f, 0.04f, 1.96f);
        this.shooter = shooter;
    }

    @Override
    public Entity getShooter() {
        return this.shooter;
    }

}
