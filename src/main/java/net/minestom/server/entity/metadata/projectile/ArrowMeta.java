package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.Nullable;

public final class ArrowMeta extends AbstractArrowMeta implements ObjectDataProvider, ProjectileMeta {
    private @Nullable Entity shooter;

    public ArrowMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        return get(MetadataDef.Arrow.COLOR);
    }

    public void setColor(int value) {
        set(MetadataDef.Arrow.COLOR, value);
    }

    @Override
    @Nullable
    public Entity getShooter() {
        return this.shooter;
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
