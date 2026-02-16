package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.Nullable;

public sealed abstract class AbstractWindChargeMeta extends EntityMeta implements ObjectDataProvider, ProjectileMeta permits BreezeWindChargeMeta, WindChargeMeta {
    private @Nullable Entity shooter;

    protected AbstractWindChargeMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    public @Nullable Entity getShooter() {
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
