package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.Nullable;

public class ArrowMeta extends AbstractArrowMeta implements ObjectDataProvider, ProjectileMeta {
    private MetaTarget shooter;

    public ArrowMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        return metadata.get(MetadataDef.Arrow.COLOR);
    }

    public void setColor(int value) {
        metadata.set(MetadataDef.Arrow.COLOR, value);
    }

    @Override
    @Nullable
    public MetaTarget getShooter() {
        return this.shooter;
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
