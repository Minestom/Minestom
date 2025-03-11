package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class ProjectileEntityMeta extends EntityMeta implements ProjectileMeta {
    private WeakReference<Entity> shooterRef;

    public ProjectileEntityMeta(@Nullable Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    public @Nullable Entity getShooter() {
        return unwrap(this.shooterRef);
    }

    @Override
    public void setShooter(@Nullable Entity shooter) {
        this.shooterRef = wrap(shooter);
    }
}
