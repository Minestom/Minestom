package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.Nullable;

public class AbstractWindChargeMeta extends EntityMeta implements ObjectDataProvider, ProjectileMeta {
    private @Nullable MetaTarget shooter;

    public AbstractWindChargeMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    public @Nullable MetaTarget getShooter() {
        return shooter;
    }

    @Override
    public void setShooter(@Nullable MetaTarget shooter) {
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
