package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WitherSkullMeta extends ProjectileEntityMeta implements ObjectDataProvider {
    public WitherSkullMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isInvulnerable() {
        return metadata.get(MetadataDef.WitherSkull.IS_INVULNERABLE);
    }

    public void setInvulnerable(boolean value) {
        metadata.set(MetadataDef.WitherSkull.IS_INVULNERABLE, value);
    }

    @Override
    public int getObjectData() {
        final var shooter = getShooter();

        return shooter == null ? 0 : shooter.getEntityId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
