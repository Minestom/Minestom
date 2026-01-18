package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.Nullable;

public final class WitherSkullMeta extends EntityMeta implements ObjectDataProvider, ProjectileMeta {
    private @Nullable Entity shooter;

    public WitherSkullMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isInvulnerable() {
        return metadata.get(MetadataDef.WitherSkull.IS_INVULNERABLE);
    }

    public void setInvulnerable(boolean value) {
        metadata.set(MetadataDef.WitherSkull.IS_INVULNERABLE, value);
    }

    @Override
    @Nullable
    public Entity getShooter() {
        return shooter;
    }

    @Override
    public void setShooter(@Nullable Entity shooter) {
        this.shooter = shooter;
    }

    @Override
    public int getObjectData() {
        return this.shooter == null ? 0 : this.shooter.getEntityId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
